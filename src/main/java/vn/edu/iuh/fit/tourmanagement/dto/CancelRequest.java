package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CancelRequest {
    private String reason;      // Lý do hủy
    private LocalDateTime cancelDate;  // Ngày hủy
    private boolean isHoliday;  // Có phải ngày lễ/tết không?
}
