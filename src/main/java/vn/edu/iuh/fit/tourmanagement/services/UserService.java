package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

}
