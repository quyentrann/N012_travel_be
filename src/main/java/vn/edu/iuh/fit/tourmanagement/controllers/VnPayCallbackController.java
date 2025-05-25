package vn.edu.iuh.fit.tourmanagement.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.enums.RefundStatus;
import vn.edu.iuh.fit.tourmanagement.models.BookingHistory;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.repositories.BookingHistoryRepository;
import vn.edu.iuh.fit.tourmanagement.services.TourBookingService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/payment")
public class VnPayCallbackController {
    private static final Logger logger = LoggerFactory.getLogger(VnPayCallbackController.class);

    private final TourBookingService tourBookingService;

    @Autowired
    private BookingHistoryRepository bookingHistoryRepository;

    @Value("${vnpay.secretKey}")
    private String secretKey;

    public VnPayCallbackController(TourBookingService tourBookingService) {
        this.tourBookingService = tourBookingService;
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, String>> vnpayReturn(
            @RequestParam Map<String, String> params,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        logger.info("VNPAY callback received with params: {}", params);

        // Kiểm tra người dùng đã xác thực
        if (userDetails == null) {
            logger.error("No authenticated user for VNPAY callback");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Chưa đăng nhập. Vui lòng đăng nhập để tiếp tục."));
        }

        // Kiểm tra các tham số VNPAY bắt buộc
        if (!params.containsKey("vnp_SecureHash") || !params.containsKey("vnp_TxnRef") ||
                !params.containsKey("vnp_ResponseCode") || !params.containsKey("vnp_TransactionStatus") ||
                !params.containsKey("vnp_TransactionNo")) {
            logger.error("Missing required VNPAY parameters: {}", params);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Thiếu thông tin thanh toán từ VNPAY"));
        }

        // Kiểm tra tính toàn vẹn dữ liệu
        String vnp_SecureHash = params.get("vnp_SecureHash");
        String calculatedHash = calculateSecureHash(params);
        if (!vnp_SecureHash.equalsIgnoreCase(calculatedHash)) {
            logger.warn("Invalid VNPAY secure hash for vnp_TxnRef: {}. Expected: {}, Received: {}, Input: {}",
                    params.get("vnp_TxnRef"), calculatedHash, vnp_SecureHash, params);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Xác thực dữ liệu không hợp lệ"));
        }

        // Lấy bookingId từ vnp_TxnRef
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
        String vnp_TransactionNo = params.get("vnp_TransactionNo");
        String vnp_TxnRef = params.get("vnp_TxnRef");
        Long bookingId;
        try {
            bookingId = Long.parseLong(vnp_TxnRef.split("_")[0]);
        } catch (NumberFormatException e) {
            logger.error("Invalid vnp_TxnRef format: {}", vnp_TxnRef);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "ID booking không hợp lệ"));
        }

        try {
            // Kiểm tra booking tồn tại
            Optional<TourBooking> optionalBooking = tourBookingService.getBookingById(bookingId);
            if (optionalBooking.isEmpty()) {
                logger.warn("Booking not found for ID: {}", bookingId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Không tìm thấy booking"));
            }

            TourBooking booking = optionalBooking.get();

            // Kiểm tra quyền sở hữu booking
            String customerIdentifier = booking.getCustomer().getUser().getUsername();
            if (!customerIdentifier.equals(userDetails.getUsername())) {
                logger.warn("User {} does not own booking {} (owned by {})",
                        userDetails.getUsername(), bookingId, customerIdentifier);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Bạn không có quyền truy cập booking này"));
            }

            logger.info("Booking ID: {}, Current Status: {}, TransactionNo: {}",
                    bookingId, booking.getStatus(), vnp_TransactionNo);

            // Kiểm tra giao dịch đã xử lý chưa
            Optional<BookingHistory> existingHistory = bookingHistoryRepository
                    .findByBooking_BookingIdAndReasonContaining(bookingId, "Transaction: " + vnp_TransactionNo);
            if (existingHistory.isPresent()) {
                logger.info("Transaction {} for booking {} already processed", vnp_TransactionNo, bookingId);
                return ResponseEntity.ok(Map.of("message", "Giao dịch đã được xử lý thành công!"));
            }

            // Kiểm tra trạng thái booking
            if (booking.getStatus() != BookingStatus.PENDING_PAYMENT &&
                    booking.getStatus() != BookingStatus.CONFIRMED) {
                logger.warn("Booking {} is not in PENDING_PAYMENT or CONFIRMED status: {}",
                        bookingId, booking.getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Booking không ở trạng thái cho phép thanh toán"));
            }

            // Xử lý giao dịch VNPAY
            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                booking.setStatus(BookingStatus.PAID);
                tourBookingService.updateBooking(booking);

                // Ghi lịch sử giao dịch
                BookingHistory history = BookingHistory.builder()
                        .booking(booking)
                        .oldStatus(booking.getStatus() == BookingStatus.PENDING_PAYMENT ?
                                BookingStatus.PENDING_PAYMENT : BookingStatus.CONFIRMED)
                        .newStatus(BookingStatus.PAID)
                        .changeDate(LocalDateTime.now())
                        .reason("Thanh toán VNPAY thành công (Transaction: " + vnp_TransactionNo + ")")
                        .cancellationFee(0)
                        .refundAmount(0)
                        .additionalPayment(0)
                        .refundStatus(RefundStatus.NONE)
                        .tour(booking.getTour())
                        .isHoliday(false)
                        .cancelDate(null)
                        .build();
                bookingHistoryRepository.save(history);
                logger.info("BookingHistory saved for VNPAY payment: bookingId={}, newStatus=PAID", bookingId);

                return ResponseEntity.ok(Map.of("message", "Giao dịch thành công! Đã cập nhật trạng thái."));
            } else {
                logger.info("VNPAY transaction failed for bookingId: {}, responseCode: {}, transactionStatus: {}",
                        bookingId, vnp_ResponseCode, vnp_TransactionStatus);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Giao dịch thất bại!"));
            }
        } catch (Exception e) {
            logger.error("Error processing VNPAY callback for bookingId: {}", bookingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi xử lý thanh toán: " + e.getMessage()));
        }
    }

    private String calculateSecureHash(Map<String, String> params) {
        try {
            Map<String, String> sortedParams = new TreeMap<>(params);
            sortedParams.remove("vnp_SecureHash");

            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                    queryBuilder.append(entry.getKey()).append('=').append(value).append('&');
                }
            }
            if (queryBuilder.length() > 0) {
                queryBuilder.deleteCharAt(queryBuilder.length() - 1);
            }

            logger.debug("Secure hash input: {}", queryBuilder.toString());
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(keySpec);
            byte[] hashBytes = sha512Hmac.doFinal(queryBuilder.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hashHex = new StringBuilder();
            for (byte b : hashBytes) {
                hashHex.append(String.format("%02x", b));
            }
            logger.debug("Calculated secure hash: {}", hashHex.toString());
            return hashHex.toString();
        } catch (Exception e) {
            logger.error("Error calculating VNPAY secure hash: {}", e.getMessage());
            throw new RuntimeException("Error calculating secure hash", e);
        }
    }
}