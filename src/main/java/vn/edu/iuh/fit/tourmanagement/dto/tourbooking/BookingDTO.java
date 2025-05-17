package vn.edu.iuh.fit.tourmanagement.dto.tourbooking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Long bookingId;
    private String customerName;
    private String tourName;
    private LocalDateTime bookingDate;
    private int numberPeople;
    private double totalPrice;
    private BookingStatus status;
}
