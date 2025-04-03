package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class VnPayCallbackController {

    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, String>> vnpayReturn(@RequestParam Map<String, String> params) {
        // In ra tất cả tham số để debug
        params.forEach((key, value) -> System.out.println(key + ": " + value));

        // Kiểm tra kết quả thanh toán
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TransactionStatus = params.get("vnp_TransactionStatus");

        if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
            return ResponseEntity.ok(Map.of("message", "Giao dịch thành công!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Giao dịch thất bại!"));
        }
    }
}
