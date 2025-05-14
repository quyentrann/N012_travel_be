package vn.edu.iuh.fit.tourmanagement.dto.report;

import lombok.Data;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

@Data
public class BookingDetail {
    private TourBooking booking;
    private Customer customer;
    private Tour tour;

    public BookingDetail(TourBooking booking, Customer customer, Tour tour) {
        this.booking = booking;
        this.customer = customer;
        this.tour = tour;
    }
}
