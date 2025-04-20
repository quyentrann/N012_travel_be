package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;

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
    private String status;
    private TourDTO tour; // Thêm TourDTO
}