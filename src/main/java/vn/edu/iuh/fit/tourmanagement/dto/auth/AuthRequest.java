package vn.edu.iuh.fit.tourmanagement.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.UserRole;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.User;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {

    private Long id;
    private String email;
    private String fullName;
    private String password;
    private String token;
    private String refreshToken;
    private Long customerId;
    private LocalDate DOB;
    private String address;
    private boolean gender;
    private UserRole role;
    private UserStatus status;
}
