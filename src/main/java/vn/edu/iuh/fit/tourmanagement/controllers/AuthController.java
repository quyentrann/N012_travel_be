package vn.edu.iuh.fit.tourmanagement.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.tourmanagement.dto.AuthRequest;
import vn.edu.iuh.fit.tourmanagement.dto.auth.AuthResponse;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;
import vn.edu.iuh.fit.tourmanagement.services.AuthJWTService;
import vn.edu.iuh.fit.tourmanagement.services.MailService;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthJWTService authService;

    @Autowired
    private MailService mailService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request);

        if (response == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder().message("Email already exists").build());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestParam("email") String email,
                                              @RequestParam("password") String password) {
        System.out.println(password);
        return ResponseEntity.ok(authService.authentication(email, password));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestParam("email") String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder().message("Email không tồn tại trong hệ thống.").build());
        }

        boolean sent = mailService.sendOtpEmail(email);
        if (sent) {
            return ResponseEntity.ok(AuthResponse.builder().message("OTP đã được gửi đến email.").build());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder().message("Không thể gửi OTP.").build());
        }
    }

    // Xác minh OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestParam("email") String email,
                                                  @RequestParam("otp") String otp) {
        boolean isValid = mailService.verifyOtp(email, otp);
        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder().message("OTP sai hoặc đã hết hạn.").build());
        }
        return ResponseEntity.ok(AuthResponse.builder().message("OTP hợp lệ. Bạn có thể đặt lại mật khẩu.").build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestParam("email") String email,
                                                      @RequestParam("newPassword") String newPassword) {
        AuthResponse response = authService.resetPassword(email, newPassword);
        mailService.removeOtp(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody String refreshToken) {
        try {
            return ResponseEntity.ok(authService.refreshToken(refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().build());
        }
    }
}