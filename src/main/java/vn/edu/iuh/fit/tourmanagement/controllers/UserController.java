package vn.edu.iuh.fit.tourmanagement.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.services.AuthService;
import vn.edu.iuh.fit.tourmanagement.services.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;


    // Lấy tất cả người dùng
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getListUser();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Lấy người dùng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Lấy email từ token

        User user = userService.getUserByEmail(email); // Hàm này cần implement
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    // Tạo người dùng mới
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Cập nhật thông tin người dùng
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName(); // Lấy email từ token

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng!");
            }

            Customer customer = user.getCustomer();
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy thông tin khách hàng!");
            }

            // Cập nhật các trường của Customer
            if (updates.containsKey("fullName")) {
                customer.setFullName((String) updates.get("fullName"));
            }
            if (updates.containsKey("phoneNumber")) {
                customer.setPhoneNumber((String) updates.get("phoneNumber"));
            }
            if (updates.containsKey("address")) {
                customer.setAddress((String) updates.get("address"));
            }
            if (updates.containsKey("dob")) {
                String dobString = (String) updates.get("dob");
                customer.setDob(LocalDate.parse(dobString)); // Parse chuỗi ngày thành LocalDate
            }
            if (updates.containsKey("gender")) {
                customer.setGender(Boolean.parseBoolean(updates.get("gender").toString())); // Parse gender thành boolean
            }

            // Cập nhật email của User (nếu có)
            if (updates.containsKey("email")) {
                String newEmail = (String) updates.get("email");
                if (userService.existsByEmail(newEmail) && !newEmail.equals(email)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email đã được sử dụng!");
                }
                user.setEmail(newEmail);
            }

            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cập nhật hồ sơ thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile file, HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng!");
            }

            Customer customer = user.getCustomer();
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy thông tin khách hàng!");
            }

            // Lưu file ảnh vào server
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get("uploads/avatars/" + fileName);
            System.out.println("Saving file to: " + filePath.toAbsolutePath());
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            if (!Files.exists(filePath)) {
                System.err.println("File not created: " + filePath);
                throw new IOException("Failed to create file");
            }

            // Tạo URL tuyệt đối từ request
            String avatarUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath("/avatars/" + fileName)
                    .build()
                    .toUriString();
            System.out.println("Generated avatarUrl: " + avatarUrl);
            customer.setAvatarUrl(avatarUrl);

            userService.updateUser(user);

            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lưu ảnh: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tải ảnh thất bại: " + e.getMessage());
        }
    }

    // Xóa người dùng (đánh dấu là đã xóa)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") Long id) {
        boolean isDeleted = userService.deleteUser(id);
        if (!isDeleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    //Đăng nhập nhưng chưa sử dụng security/ JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest){
        System.out.println("Username: " + loginRequest.get("email"));
        System.out.println("Password: " + loginRequest.get("password"));
        try{
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            User user = authService.login(email, password);

            Map<String, String> response = new HashMap<>();
            response.put("userId", String.valueOf(user.getId()));
            response.put("role", user.getRole().name());
            response.put("status", user.getStatus().name());

            return ResponseEntity.ok(response);
        }catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PatchMapping("/status/{id}")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            userService.partialUpdateUserStatus(id, updates);
            return ResponseEntity.ok("User updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
}
