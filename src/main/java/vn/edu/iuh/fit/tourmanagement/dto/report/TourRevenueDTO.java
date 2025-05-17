package vn.edu.iuh.fit.tourmanagement.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourRevenueDTO {
    private Long tourId;
    private String tourName;
    private Long totalBookings;
    private Long totalCancelled;
    private Double totalRevenue;
    private Double avgRevenuePerBooking;
//    private String mostCommonStatus;
}
