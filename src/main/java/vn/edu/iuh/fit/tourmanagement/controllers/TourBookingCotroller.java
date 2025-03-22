package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.services.TourBookingService;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class TourBookingCotroller {
    @Autowired
    private TourBookingService tourBookingService;

    @GetMapping
    public Object getAllTourBooking() {
        return tourBookingService.getListTourBooking();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourBooking> getTourBookingById(@PathVariable Long id) {
        TourBooking tourBooking = tourBookingService.getTourBookingById(id);
        if (tourBooking == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tourBooking, HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TourBooking>> getTourBookingByCustomerId(@PathVariable Long customerId) {
        List<TourBooking> tourBookings = tourBookingService.getTourBookingByCustomerId(customerId);
        if (tourBookings.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tourBookings, HttpStatus.OK);
    }
}
