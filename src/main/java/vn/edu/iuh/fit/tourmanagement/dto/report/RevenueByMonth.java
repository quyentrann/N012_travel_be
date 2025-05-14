package vn.edu.iuh.fit.tourmanagement.dto.report;

import lombok.Data;

@Data
public class RevenueByMonth {
    private String orderMonth;
    private long revenue;

    public RevenueByMonth(String orderMonth, long revenue) {
        this.orderMonth = orderMonth;
        this.revenue = revenue;
    }
}
