package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.auth.AuthResponse;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;
import vn.edu.iuh.fit.tourmanagement.services.JWTService;
import vn.edu.iuh.fit.tourmanagement.services.MailService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private MailService mailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;


    // API gửi OTP
    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) return ResponseEntity.badRequest().body("User not found.");

        User user = userOptional.get();
        if (user.getStatus() != UserStatus.PENDING) {
            return ResponseEntity.badRequest().body("Account is already activated.");
        }

        return mailService.sendOtpEmail(email) ?
                ResponseEntity.ok("OTP has been sent to your email.") :
                ResponseEntity.badRequest().body("OTP is still valid, please check your email.");
    }


//    @PostMapping("/send")
//    public ResponseEntity<String> sendOtp(@RequestParam String email) {
//        try {
//            mailService.sendOtpEmail(email);
//            return ResponseEntity.ok("OTP has been sent to your email.");
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Error sending OTP: " + e.getMessage());
//        }
//    }

    // API kiểm tra OTP
    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (mailService.verifyOtp(email, otp)) {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);

                // Xóa OTP sau khi xác thực thành công
                mailService.removeOtp(email);

                return ResponseEntity.ok("OTP is valid. Account activated.");
            }
        }
        return ResponseEntity.status(400).body("Invalid or expired OTP.");
    }

}


