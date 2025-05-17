package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTourRequest {
    private LocalDate departureDate; // Ngày khởi hành mới
    private Integer numberAdults; // Số người lớn
    private Integer numberChildren; // Số trẻ em
    private Integer numberInfants; // Số trẻ nhỏ
    private boolean isHoliday; // Có phải dịp lễ không?
    private LocalDateTime changeDate; // Thời điểm yêu cầu đổi (mặc định: hiện tại)
}