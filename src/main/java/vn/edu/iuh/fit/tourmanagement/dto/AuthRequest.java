package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String email;
    private String password;
    private String fullName;      // Họ Tên
    private String phoneNumber;   // Số điện thoại
    private LocalDate dob;        // Ngày sinh
    private String address;       // Địa chỉ
    private Boolean gender;
}