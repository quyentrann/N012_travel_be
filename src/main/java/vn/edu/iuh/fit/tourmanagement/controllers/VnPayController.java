package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.services.VnPayService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class VnPayController {

    private final VnPayService vnPayService;

    public VnPayController(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @PostMapping("/vnpay-create")
    public ResponseEntity<Map<String, String>> createPayment(@RequestBody Map<String, Object> request) {
        Long bookingId = Long.valueOf(request.get("bookingId").toString());
        Long totalPrice = Long.valueOf(request.get("totalPrice").toString());
        String ipAddress = "127.0.0.1"; // Lấy địa chỉ IP thật trong request

        String paymentUrl = vnPayService.createPayment(bookingId, totalPrice, ipAddress);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);

        return ResponseEntity.ok(response);
    }
}
