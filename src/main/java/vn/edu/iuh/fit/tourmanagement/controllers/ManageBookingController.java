package vn.edu.iuh.fit.tourmanagement.controllers;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.report.BookingDetail;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.services.ManageBookingService;
import vn.edu.iuh.fit.tourmanagement.services.TourBookingService;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/manage-bookings")
public class ManageBookingController {
    @Autowired
    private ManageBookingService bookingService;
    @Autowired
    private TourService tourService;

    @GetMapping
    public Object getAllTourBooking() {
        return bookingService.getListTourBooking();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetail> getTourBookingById(@PathVariable Long id) {
        BookingDetail tourBooking = bookingService.getTourBookingById(id);
        if (tourBooking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(tourBooking);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getTourBookingByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(bookingService.getTourBookingByCustomerId(customerId));
    }
    @GetMapping("/tour/{tourId}")
    public ResponseEntity<?> getTourBookingByTourId(
            @PathVariable Long tourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {

//        // Nếu không có ngày, gọi method cũ (không lọc theo ngày)
//        if (bookingDate == null) {
//            return ResponseEntity.ok(bookingService.getTourBookingByTourId(tourId));
//        }

        // Nếu có ngày, gọi method lọc theo ngày
        return ResponseEntity.ok(bookingService.getTourBookingByTourIdAndDate(tourId, departureDate));
    }

    @GetMapping("/tour/{tourId}/date-details")
    public ResponseEntity<?> getStartDateTourDetailByTourId(@PathVariable Long tourId) {
        return ResponseEntity.ok(bookingService.getStartDateTourDetailByTourId(tourId));
    }
}