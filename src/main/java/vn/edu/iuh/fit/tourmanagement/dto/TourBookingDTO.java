package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TourBookingDTO {
    private Long bookingId;
    private int numberPeople;
    private double totalPrice;
    private LocalDateTime bookingDate;
    private String status;
    private String tourName;
    private String tourImage;
}