package vn.edu.iuh.fit.tourmanagement.services;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.dto.AuthRequest;
import vn.edu.iuh.fit.tourmanagement.dto.auth.AuthResponse;
import vn.edu.iuh.fit.tourmanagement.enums.UserRole;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.CustomerRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.util.*;

@Service
public class AuthJWTService {

    private long EXPIRATION_TIME_ACCESS_TOKEN=86400000;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MailService mailService;

    public AuthJWTService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(AuthRequest request) {
        // Kiểm tra dữ liệu đầu vào
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() || !request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return AuthResponse.builder().message("Email không hợp lệ hoặc để trống.").build();
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return AuthResponse.builder().message("Mật khẩu phải có ít nhất 6 ký tự.").build();
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            return AuthResponse.builder().message("Họ tên không được để trống.").build();
        }
        if (request.getPhoneNumber() == null || !request.getPhoneNumber().matches("^\\d{10}$")) {
            return AuthResponse.builder().message("Số điện thoại phải có 10 chữ số.").build();
        }
        if (request.getDob() != null && request.getDob().isAfter(LocalDate.now())) {
            return AuthResponse.builder().message("Ngày sinh phải là ngày trong quá khứ.").build();
        }

        // Kiểm tra email đã tồn tại
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            User dbUser = existingUser.get();
            if (dbUser.getStatus() == UserStatus.PENDING) {
                try {
                    mailService.sendOtpEmail(request.getEmail());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return AuthResponse.builder()
                        .message("OTP đã được gửi lại. Vui lòng hoàn tất xác thực để kích hoạt tài khoản.")
                        .build();
            }
            return AuthResponse.builder()
                    .message("Tài khoản đã tồn tại.")
                    .build();
        }

        // Tạo User mới
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.PENDING)
                .build();
        newUser = userRepository.save(newUser);

        // Tạo Customer
        Customer newCustomer = Customer.builder()
                .user(newUser)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .dob(request.getDob())
                .address(request.getAddress())
                .gender(request.isGender())
                .build();
        customerRepository.save(newCustomer);

        try {
            mailService.sendOtpEmail(request.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return AuthResponse.builder()
                .message("User registered. OTP sent to email.")
                .build();
    }

    public AuthResponse resetPassword(String email, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return AuthResponse.builder().message("Email không tồn tại.").build();
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return AuthResponse.builder().message("Đặt lại mật khẩu thành công.").build();
    }


    private Map<String, String> otpStorage = new HashMap<>(); // Map<email, otp>

    public AuthResponse verifyOtp(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return AuthResponse.builder().message("OTP sai hoặc đã hết hạn.").build();
        }
        return AuthResponse.builder().message("OTP hợp lệ. Bạn có thể đặt lại mật khẩu.").build();
    }

    public void storeOtp(String email, String otp) {
        otpStorage.put(email, otp);
    }



//    public AuthResponse authentication(String email, String password) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new NoSuchElementException("Tài khoản không tồn tại"));
//
//        // Kiểm tra trạng thái tài khoản khi đăng nhập
//        if (user.getStatus() == UserStatus.PENDING) {
//            return AuthResponse.builder()
//                    .message("Tài khoản chưa được xác thực. Vui lòng kiểm tra email để hoàn tất xác thực.")
//                    .build();
//        }
//
//        if (user.getStatus() == UserStatus.DISABLED) {
//            return AuthResponse.builder()
//                    .message("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ với chúng tôi để giải quyết.")
//                    .build();
//        }
//
//        if (user.getStatus() == UserStatus.BLOCKED) {
//            return AuthResponse.builder()
//                    .message("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ với chúng tôi để giải quyết.")
//                    .build();
//        }
//
//        // Kiểm tra mật khẩu
//        if (!password.equals(user.getPassword())) {  // Sử dụng một phương pháp mã hóa mật khẩu trong thực tế
//            return AuthResponse.builder()
//                    .message("Mật khẩu không đúng. Vui lòng thử lại.")
//                    .build();
//        }
//
//        // Xử lý đăng nhập nếu tài khoản ACTIVE và mật khẩu đúng
//        String refreshToken = jwtService.generateRefreshToken(user);
//        String token = jwtService.generateToken(user);
//
//        return AuthResponse.builder()
//                .token(token)
//                .refreshToken(refreshToken)
//                .user(user)
//                .exp(new Date(System.currentTimeMillis() + EXPIRATION_TIME_ACCESS_TOKEN).getTime())
//                .build();
//    }


    public AuthResponse authentication(String email, String password) {
        System.out.println(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        ));
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new NoSuchElementException("user not found"));
        System.out.println(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(user)
                .exp(new Date(System.currentTimeMillis() + EXPIRATION_TIME_ACCESS_TOKEN).getTime())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) throws Exception {
        try {
            String email = jwtService.extractUserName(refreshToken);

            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
                throw new Exception("User not found");
            }

            User user = optionalUser.get();
            if (jwtService.isRefreshTokenValid(refreshToken, user)) {
                String token = jwtService.generateToken(user);

                return AuthResponse.builder()
                        .token(token)
                        .refreshToken(refreshToken)
                        .user(user)
                        .exp(new Date(System.currentTimeMillis() + EXPIRATION_TIME_ACCESS_TOKEN).getTime())
                        .build();
            } else {
                throw new Exception("Invalid refresh token");
            }
        } catch (Exception exception) {
            throw new Exception("Token refresh failed: " + exception.getMessage());
        }
    }
}
