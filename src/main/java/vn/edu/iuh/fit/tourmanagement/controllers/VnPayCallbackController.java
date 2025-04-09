package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.services.TourBookingService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class VnPayCallbackController {
    private final TourBookingService tourBookingService;

    public VnPayCallbackController(TourBookingService tourBookingService) {
        this.tourBookingService = tourBookingService;
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, String>> vnpayReturn(@RequestParam Map<String, String> params) {
        // In ra tất cả tham số để debug
        params.forEach((key, value) -> System.out.println(key + ": " + value));

        // Lấy thông tin từ phản hồi của VNPAY
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
        Long bookingId = Long.valueOf(params.get("vnp_TxnRef")); // Lấy bookingId từ phản hồi

        if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
            Optional<TourBooking> optionalBooking = tourBookingService.getBookingById(bookingId);

            if (optionalBooking.isPresent()) {
                TourBooking booking = optionalBooking.get();
                booking.setStatus(BookingStatus.PAID); // Cập nhật trạng thái đã thanh toán
                tourBookingService.updateBooking(booking);
                return ResponseEntity.ok(Map.of("message", "Giao dịch thành công! Đã cập nhật trạng thái."));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy booking!"));
            }
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Giao dịch thất bại!"));
    }
}
