package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long bookingId;
    private int numberPeople;
    private double totalPrice;
    private LocalDateTime bookingDate;
    private String status;
}