package vn.edu.iuh.fit.tourmanagement.dto.report;

import lombok.Data;

@Data
public class BookingStats {
    private long totalOrders;
    private long totalRevenue;
    private long totalCancelled;

    public BookingStats(long totalOrders, long totalRevenue, long totalCancelled) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.totalCancelled = totalCancelled;
    }
}

