package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelResponse {
    private String message; // Thông báo hủy thành công
    private double cancellationFee; // Phí hủy
    private double refundAmount; // Số tiền hoàn lại
}
