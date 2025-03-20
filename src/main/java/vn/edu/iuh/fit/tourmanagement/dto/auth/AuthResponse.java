package vn.edu.iuh.fit.tourmanagement.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.models.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    private Long exp;
    @JsonIgnoreProperties({"authorities", "accountNonExpired", "credentialsNonExpired","accountNonLocked"})
    private User user;
}

