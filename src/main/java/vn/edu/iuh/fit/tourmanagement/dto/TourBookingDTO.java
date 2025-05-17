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
    private Integer numberPeople;
    private Double totalPrice;
    private LocalDateTime bookingDate;
    private LocalDate departureDate;
    private String status;
    private TourDTO tour; // Thêm TourDTO
}
