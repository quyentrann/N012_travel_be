package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTourResponse {
    private String message;
    private double changeFee; // Phí đổi lịch
    private double priceDifference; // Chênh lệch giá (dương: trả thêm, âm: hoàn lại)
    private double newTotalPrice; // Tổng giá mới (bao gồm phí đổi)
    private double refundAmount; // Số tiền hoàn lại (nếu có)
}