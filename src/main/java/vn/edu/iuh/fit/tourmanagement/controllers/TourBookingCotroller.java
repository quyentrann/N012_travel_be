package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.TourBookingDTO;
import vn.edu.iuh.fit.tourmanagement.dto.TourBookingRequest;
import vn.edu.iuh.fit.tourmanagement.models.BookingHistory;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.services.TourBookingService;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/bookings")
public class TourBookingCotroller {
    @Autowired
    private TourBookingService tourBookingService;

    @Autowired
    private TourService tourService;

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

    @PostMapping("/book")
    public ResponseEntity<?> bookTour(@RequestBody TourBookingRequest request, Authentication authentication) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Invalid request!");
        }

        try {
            TourBooking booking = tourBookingService.bookTour(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @GetMapping("/history")
    public ResponseEntity<List<TourBookingDTO>> getBookingHistoryByCustomer(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        User user = (User) authentication.getPrincipal();

        if (user.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<TourBooking> bookings = tourBookingService.getTourBookingByCustomerId(user.getCustomer().getCustomerId());

        if (bookings == null || bookings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<TourBookingDTO> bookingDTOs = bookings.stream().map(booking ->
                new TourBookingDTO(
                        booking.getBookingId(),
                        booking.getNumberPeople(),
                        booking.getTotalPrice(),
                        booking.getBookingDate(),
                        booking.getStatus().toString(),
                        booking.getTour().getName(),
                        booking.getTour().getImageURL()
                )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(bookingDTOs);
    }

}
