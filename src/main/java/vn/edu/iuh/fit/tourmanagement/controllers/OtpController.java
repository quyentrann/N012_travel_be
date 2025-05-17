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
        System.out.println("Received resend OTP request for email: " + email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            System.out.println("User not found for email: " + email);
            return ResponseEntity.badRequest().body("User not found.");
        }

        User user = userOptional.get();
        System.out.println("User status: " + user.getStatus());
        if (user.getStatus() != UserStatus.PENDING) {
            System.out.println("Account is already activated for email: " + email);
            return ResponseEntity.badRequest().body("Account is already activated. Please log in or reset your password.");
        }

        // Xóa OTP cũ trước khi gửi OTP mới
        mailService.removeOtp(email);

        boolean sent = mailService.sendOtpEmail(email);
        if (sent) {
            System.out.println("OTP sent successfully to email: " + email);
            return ResponseEntity.ok("OTP has been sent to your email.");
        } else {
            System.out.println("Failed to send OTP to email: " + email);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP.");
        }
    }

    // API kiểm tra OTP
    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        System.out.println("Verifying OTP: email=" + email + ", otp=" + otp);
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

    // API gửi OTP cho quên mật khẩu
    @PostMapping("/send-forgot-password-otp")
    public ResponseEntity<String> sendForgotPasswordOtp(@RequestParam String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Email not found.");
        }

        System.out.println("Removing old OTP for email: " + email);
        mailService.removeOtp(email);

        boolean otpSent = mailService.sendOtpEmail(email);
        if (otpSent) {
            return ResponseEntity.ok("OTP has been sent to your email.");
        } else {
            return ResponseEntity.badRequest().body("Unable to send OTP.");
        }
    }



}


