package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.dto.auth.AuthRequest;
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

    public AuthJWTService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(AuthRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        User newUser = userRepository.save(user);
        Customer customer = customerRepository.save(Customer.builder()
                .fullName(request.getFullName())
                .DOB(request.getDOB())
                .address(request.getAddress())
                .user(user)
                .gender(request.isGender())
                .build());

        Map<String, Object> claims = new HashMap<>();
        claims.put("user", customer.toString());

        String refreshToken = jwtService.generateRefreshToken(claims,newUser);
        String token = jwtService.generateToken(claims,newUser);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(newUser)
                .exp(new Date(System.currentTimeMillis() + EXPIRATION_TIME_ACCESS_TOKEN).getTime())
                .build();
    }

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
