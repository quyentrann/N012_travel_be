package vn.edu.iuh.fit.tourmanagement.dto.report;

import lombok.Data;

@Data
public class OrderCountByStatus {
    private String status;
    private long quantity;

    public OrderCountByStatus(String status, long quantity) {
        this.status = status;
        this.quantity = quantity;
    }
}
