// vn.edu.iuh.fit.tourmanagement.dto.BookingHistoryDTO.java
package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingHistoryDTO {
    private Long id;
    private Long bookingId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changeDate;
    private String reason;
    private double cancellationFee;
    private double refundAmount;
    private double additionalPayment;
    private String refundStatus;
    private LocalDateTime cancelDate;
    private boolean isHoliday;
}