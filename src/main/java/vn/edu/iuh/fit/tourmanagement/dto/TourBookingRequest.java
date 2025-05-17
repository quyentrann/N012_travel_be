package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourBookingRequest {
    private Long tourId;            // ID của tour khách hàng chọn
    private int numberPeople;       // Số lượng người tham gia tour
    private double totalPrice;
    private String fullName;        // Họ tên
    private String phoneNumber;
    private int numberAdults;       // Số lượng người lớn
    private int numberChildren;     // Số lượng trẻ em
    private int numberInfants;      // Số lượng trẻ nhỏ
    private LocalDate departureDate;
}