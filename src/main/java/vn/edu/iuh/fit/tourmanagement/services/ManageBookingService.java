package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.dto.report.BookingDetail;
import vn.edu.iuh.fit.tourmanagement.dto.tourbooking.BookingDTO;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ManageBookingService {
    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private TourRepository tourRepository;

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

    public List<BookingDTO> getTourBookingByTourIdAndDate(Long tourId, LocalDate bookingDate) {
        List<TourBooking> tourBookings = tourBookingRepository.findByTour_TourIdAndDepartureDate(tourId, bookingDate);

        return tourBookings.stream().map(tourBooking -> {
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
    }


    public List<LocalDate> getStartDateTourDetailByTourId(Long tourId) {
        List<LocalDate> startDates ;
        Tour tour = tourRepository.findById(tourId).orElse(null);
        if (tour != null) {
            startDates = tour.getTourDetails().stream()
                    .map(detail -> detail.getStartDate())
                    .distinct()
                    .toList();
        } else {
            startDates = List.of(); // Trả về danh sách rỗng nếu tour không tồn tại
        }
        return startDates;
    }

}
