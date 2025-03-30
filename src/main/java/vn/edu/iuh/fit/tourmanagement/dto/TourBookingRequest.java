package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.Discount;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourBookingRequest {
    private Long tourId;            // ID của tour khách hàng chọn
//    private LocalDateTime bookingDate;
//    private Long customerId;
    private int numberPeople;       // Số lượng người tham gia tour
//    private Long discountCode;
    private double totalPrice;
}