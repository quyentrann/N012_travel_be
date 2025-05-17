package vn.edu.iuh.fit.tourmanagement.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetail {
    private TourBooking booking;
    private Customer customer;
//    private Tour tour;
    private String tourName;
    private String tourLocation;
    private String tourDescription;
    private String categoryName;
    private Double price;

//    public BookingDetail(TourBooking booking, Customer customer, Tour tour) {
//        this.booking = booking;
//        this.customer = customer;
//        this.tour = tour;
//    }
}
