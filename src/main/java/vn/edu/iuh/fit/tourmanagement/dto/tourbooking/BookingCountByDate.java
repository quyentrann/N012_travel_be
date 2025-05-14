package vn.edu.iuh.fit.tourmanagement.dto.tourbooking;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingCountByDate {
    private LocalDate bookingDate;
    private Long totalPeople;
}
