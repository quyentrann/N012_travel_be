package vn.edu.iuh.fit.tourmanagement.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueTourReport {
    private String tourName;
    private int totalBookings;
//    private int totalCancelledBookings;
    private double totalRevenue;
}
