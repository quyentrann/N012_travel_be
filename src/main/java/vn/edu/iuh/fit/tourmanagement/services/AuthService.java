package vn.edu.iuh.fit.tourmanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;

import java.util.Optional;

@Service
//@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;


    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String username, String password) {
        Optional<User> userOptional = userRepository.findByEmailAndPassword(username, password);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;  // Sai tài khoản hoặc mật khẩu
    }
}
