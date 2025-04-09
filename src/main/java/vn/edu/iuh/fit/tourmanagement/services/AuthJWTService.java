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
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            User dbUser = existingUser.get();

            // Nếu tài khoản là PENDING, gửi lại OTP
            if (dbUser.getStatus() == UserStatus.PENDING) {
                try {
                    mailService.sendOtpEmail(request.getEmail()); // Gửi OTP
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return AuthResponse.builder()
                        .message("OTP đã được gửi lại. Vui lòng hoàn tất xác thực để kích hoạt tài khoản.")
                        .build();
            }

            // Nếu tài khoản đã tồn tại (ACTIVE, DISABLED, BLOCKED)
            return AuthResponse.builder()
                    .message("Tài khoản đã tồn tại.")
                    .build();
        }


        // Nếu tài khoản chưa tồn tại, tạo tài khoản mới
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.PENDING) // Đặt trạng thái là PENDING
                .build();
        newUser = userRepository.save(newUser); // Đảm bảo User có ID

        // **Tạo Customer sau khi User có ID**
        Customer newCustomer = Customer.builder()
                .user(newUser)
                .build();
        customerRepository.save(newCustomer);

        try {
            mailService.sendOtpEmail(request.getEmail()); // Gửi OTP khi đăng ký thành công
        } catch (Exception e) {
            e.printStackTrace();
        }

        return AuthResponse.builder()
                .message("User registered. OTP sent to email.")
                .build();
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
