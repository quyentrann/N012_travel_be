package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourBookingDTO {
    private Long bookingId;
    private int numberPeople;
    private int numberAdults;  // Thêm trường
    private int numberChildren;  // Thêm trường
    private int numberInfants;  // Thêm trường
    private double totalPrice;
    private LocalDateTime bookingDate;
    private LocalDate departureDate;
    private String status;
    private TourDTO tour;
}
