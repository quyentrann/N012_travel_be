package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;
import vn.edu.iuh.fit.tourmanagement.services.VnPayService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class VnPayController {

    private final VnPayService vnPayService;

    @Autowired
    private TourBookingRepository tourBookingRepository;
    public VnPayController(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @PostMapping("/vnpay-create")
    public ResponseEntity<Map<String, String>> createPayment(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Chưa đăng nhập"));
        }
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Không có quyền truy cập"));
        }

        try {
            Long bookingId = Long.valueOf(request.get("bookingId").toString());
            Long totalPrice = Long.valueOf(request.get("totalPrice").toString());
            String ipAddress = request.getOrDefault("ipAddress", "127.0.0.1").toString();

            // Validate booking
            TourBooking booking = tourBookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
            if (!booking.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền thanh toán booking này"));
            }
            if (booking.getStatus() != BookingStatus.PENDING_PAYMENT &&
                    booking.getStatus() != BookingStatus.CONFIRMED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Booking không ở trạng thái cho phép thanh toán"));
            }

            String paymentUrl = vnPayService.createPayment(bookingId, totalPrice, ipAddress);

            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tạo thanh toán VNPAY"));
        }
    }
}
