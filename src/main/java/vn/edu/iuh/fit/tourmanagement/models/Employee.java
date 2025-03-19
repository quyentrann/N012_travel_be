package vn.edu.iuh.fit.tourmanagement.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "dob")
    private LocalDate DOB;

    @Column(name = "phone_number")
    private String phongNumber;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "address")
    private String address;

    @Column(name = "gender")
    private boolean gender; // Chỉ có Nam/Nữ True là name, false là nữ

    @Column(name = "cid")
    private String CID; // Chứng minh nhân dân/ CCCD

    @Column(name = "position")// true là quản lý, false là nhân viên
    private boolean position;
}
