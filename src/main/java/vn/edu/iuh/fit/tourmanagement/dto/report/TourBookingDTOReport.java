package vn.edu.iuh.fit.tourmanagement.dto.report;

import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.models.Tour;

import java.time.LocalDateTime;

//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class TourBookingDTOReport{
    private Long bookingId;
    private int numberPeople;
    private Double totalPrice;
    private LocalDateTime bookingDate;
    private String status;
    private String tourName;
    private String customerFullName;


}
