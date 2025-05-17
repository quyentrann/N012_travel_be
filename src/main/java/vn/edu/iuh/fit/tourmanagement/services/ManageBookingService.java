package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.dto.report.BookingDetail;
import vn.edu.iuh.fit.tourmanagement.dto.tourbooking.BookingDTO;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;

import java.util.List;

@Service
public class ManageBookingService {
    @Autowired
    private TourBookingRepository tourBookingRepository;

    public List<BookingDTO> getListTourBooking() {
        List<TourBooking> tourBookings = tourBookingRepository.findAll();
        List<BookingDTO> tourBookingDTOs = tourBookings.stream().map(tourBooking -> {
            Tour tour = tourBooking.getTour();
            return new BookingDTO(
                    tourBooking.getBookingId(),
                    tourBooking.getCustomer().getFullName(),
                    tour.getName(),
                    tourBooking.getBookingDate(),
                    tourBooking.getNumberPeople(),
                    tourBooking.getTotalPrice(),
                    tourBooking.getStatus()
            );
        }).toList();
        return tourBookingDTOs;
    }
    public BookingDetail getTourBookingById(Long id) {
        TourBooking tourBooking = tourBookingRepository.findById(id).orElse(null);
        Tour tour = tourBooking.getTour();
        BookingDetail bookingDetail = new BookingDetail(
                tourBooking,
                tourBooking.getCustomer(),
//                tour
                tour.getName(),
                tour.getLocation(),
                tour.getDescription(),
                tour.getTourcategory().getCategoryName(),
                tour.getPrice()
        );

        return bookingDetail;
    }

    public List<BookingDTO> getTourBookingByCustomerId(Long customerId) {
        List<TourBooking> tourBookings = tourBookingRepository.findByCustomerId(customerId);
        List<BookingDTO> tourBookingDTOs = tourBookings.stream().map(tourBooking -> {
            Tour tour = tourBooking.getTour();
            return new BookingDTO(
                    tourBooking.getBookingId(),
                    tourBooking.getCustomer().getFullName(),
                    tour.getName(),
                    tourBooking.getBookingDate(),
                    tourBooking.getNumberPeople(),
                    tourBooking.getTotalPrice(),
                    tourBooking.getStatus()
            );
        }).toList();
        return tourBookingDTOs;
    }

}
