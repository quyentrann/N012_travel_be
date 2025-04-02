package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getListUser() {
        return userRepository.findAll();
    }

    public boolean deleteUser(Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (!user.isEmpty()) {
                User userDelete = user.get();
                userDelete.setDeleted(true);
                userRepository.save(user.get());
                return true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public User updateUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public User createUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void partialUpdateUserStatus(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "status" -> user.setStatus(
                        UserStatus.valueOf(value.toString().toUpperCase())  // Chuyển String sang Enum
                );
            }
        });
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

}
