package vn.edu.iuh.fit.tourmanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import vn.edu.iuh.fit.tourmanagement.enums.UserRole;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class User extends Auditable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "password",  length = 255)
    @JsonIgnore
    private String password;

    @Column(name = "role", length = 50)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private UserStatus status;



}
