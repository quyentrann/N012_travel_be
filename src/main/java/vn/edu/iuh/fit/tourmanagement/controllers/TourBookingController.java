package vn.edu.iuh.fit.tourmanagement.controllers;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.edu.iuh.fit.tourmanagement.dto.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;
import vn.edu.iuh.fit.tourmanagement.services.TourBookingService;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/bookings")
public class TourBookingController {
    @Autowired
    private TourBookingService tourBookingService;

    @Autowired
    private TourService tourService;
    @Autowired
    private TourBookingRepository tourBookingRepository;

    @GetMapping
    public Object getAllTourBooking() {
        return tourBookingService.getListTourBooking();
    }

    private double calculateCancellationFee(TourBooking booking, LocalDateTime cancelDate, boolean isHoliday) {
        // Lấy ngày khởi hành của tour
        LocalDateTime tourStartDate = booking.getTour().getTourDetails().get(0).getStartDate().atStartOfDay();

        // Tính số ngày trước khi tour bắt đầu
        long daysBeforeTour = java.time.temporal.ChronoUnit.DAYS.between(cancelDate, tourStartDate);

        double cancellationFee = 0;
        if (isHoliday) {
            // Phí hủy cho ngày lễ
            if (daysBeforeTour >= 30) {
                cancellationFee = 0.2 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 15) {
                cancellationFee = 0.4 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                cancellationFee = 0.6 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 3) {
                cancellationFee = 0.8 * booking.getTotalPrice();
            } else {
                cancellationFee = booking.getTotalPrice(); // 100% phí
            }
        } else {
            // Phí hủy cho ngày thường
            if (daysBeforeTour >= 14) {
                cancellationFee = 0.1 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                cancellationFee = 0.3 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 4) {
                cancellationFee = 0.5 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 1) {
                cancellationFee = 0.7 * booking.getTotalPrice();
            } else {
                cancellationFee = booking.getTotalPrice(); // 100% phí
            }
        }

        return cancellationFee;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourBookingDTO> getTourBookingById(@PathVariable Long id, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        TourBooking tourBooking = tourBookingService.getTourBookingById(id);
        if (tourBooking == null || !tourBooking.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Tour tour = tourBooking.getTour();
        Hibernate.initialize(tour.getBookings()); // Fetch bookings
        TourDTO tourDTO = new TourDTO(
                tour.getTourId(),
                tour.getName(),
                tour.getPrice(),
                tour.getAvailableSlot(),
                tour.getLocation(),
                tour.getDescription(),
                tour.getHighlights(),
                tour.getImageURL(),
                tour.getExperiences(),
                tour.getStatus() != null ? tour.getStatus().toString() : null,
                tour.getTourcategory() != null ? new TourCategoryDTO(
                        tour.getTourcategory().getCategoryId(),
                        tour.getTourcategory().getCategoryName(),
                        tour.getTourcategory().getDescription()
                ) : null,
                tour.getTourDetails() != null ? tour.getTourDetails().stream().map(TourDetailDTO::new).collect(Collectors.toList()) : Collections.emptyList(),
                tour.getTourSchedules() != null ? tour.getTourSchedules().stream().map(TourScheduleDTO::new).collect(Collectors.toList()) : Collections.emptyList(),
                tour.getReviews() != null ? tour.getReviews().stream().map(ReviewDTO::new).collect(Collectors.toList()) : Collections.emptyList(),
                tour.getBookings() != null ? tour.getBookings().stream()
                        .map(booking -> new BookingDTO(
                                booking.getBookingId(),
                                booking.getNumberPeople(),
                                booking.getTotalPrice(),
                                booking.getBookingDate(),
                                booking.getStatus().toString()
                        ))
                        .collect(Collectors.toList()) : Collections.emptyList()
        );
        TourBookingDTO bookingDTO = new TourBookingDTO(
                tourBooking.getBookingId(),
                tourBooking.getNumberPeople(),
                tourBooking.getTotalPrice(),
                tourBooking.getBookingDate(),
                tourBooking.getStatus().toString(),
                tourDTO
        );
        return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
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

    @PutMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId,
                                           @RequestBody CancelRequest cancelRequest,
                                           Authentication authentication) {
        try {
            CancelResponse result = tourBookingService.cancelBooking(
                    bookingId,
                    cancelRequest.getReason(),
                    cancelRequest.getCancelDate(),
                    cancelRequest.isHoliday(),
                    authentication
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/calculate-cancellation-fee/{bookingId}")
    public ResponseEntity<CancelResponse> calculateCancellationFee(
            @PathVariable Long bookingId,
            @RequestBody CancelRequest cancelRequest,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy booking"));
        if (user.getCustomer() == null || !booking.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            throw new IllegalStateException("Bạn không có quyền xem thông tin hủy booking này.");
        }

        if (booking.getStatus() == BookingStatus.CANCELED ||
                booking.getStatus() == BookingStatus.COMPLETED ||
                booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Không thể hủy booking với trạng thái hiện tại: " + booking.getStatus());
        }

        double cancellationFee = 0;
        double refundAmount = 0;
        if (booking.getStatus() == BookingStatus.PAID) {
            cancellationFee = calculateCancellationFee(booking, cancelRequest.getCancelDate(), cancelRequest.isHoliday());
            refundAmount = booking.getTotalPrice() - cancellationFee;
        }

        CancelResponse response = new CancelResponse();
        response.setMessage("Thông tin phí hủy");
        response.setCancellationFee(cancellationFee);
        response.setRefundAmount(refundAmount);
        return ResponseEntity.ok(response);
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
        List<TourBookingDTO> bookingDTOs = bookings.stream().map(booking -> {
            Tour tour = booking.getTour();
            Hibernate.initialize(tour.getBookings());
            TourDTO tourDTO = new TourDTO(
                    tour.getTourId(),
                    tour.getName(),
                    tour.getPrice(),
                    tour.getAvailableSlot(),
                    tour.getLocation(),
                    tour.getDescription(),
                    tour.getHighlights(),
                    tour.getImageURL(),
                    tour.getExperiences(),
                    tour.getStatus() != null ? tour.getStatus().toString() : null,
                    tour.getTourcategory() != null ? new TourCategoryDTO(
                            tour.getTourcategory().getCategoryId(),
                            tour.getTourcategory().getCategoryName(),
                            tour.getTourcategory().getDescription()
                    ) : null,
                    tour.getTourDetails() != null ? tour.getTourDetails().stream().map(TourDetailDTO::new).collect(Collectors.toList()) : Collections.emptyList(),
                    tour.getTourSchedules() != null ? tour.getTourSchedules().stream().map(TourScheduleDTO::new).collect(Collectors.toList()) : Collections.emptyList(),
                    tour.getReviews() != null ? tour.getReviews().stream().map(ReviewDTO::new).collect(Collectors.toList()) : Collections.emptyList(),
                    tour.getBookings() != null ? tour.getBookings().stream()
                            .map(b -> new BookingDTO(
                                    b.getBookingId(),
                                    b.getNumberPeople(),
                                    b.getTotalPrice(),
                                    b.getBookingDate(),
                                    b.getStatus().toString()
                            ))
                            .collect(Collectors.toList()) : Collections.emptyList()
            );
            return new TourBookingDTO(
                    booking.getBookingId(),
                    booking.getNumberPeople(),
                    tour.getPrice(),
                    booking.getBookingDate(),
                    booking.getStatus().toString(),
                    tourDTO
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTOs); // Luôn trả về mảng, dù rỗng
    }

    @GetMapping("/history/{bookingId}")
    public ResponseEntity<TourBookingDetailDTO> getBookingDetailById(@PathVariable Long bookingId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            TourBookingDetailDTO bookingDetail = tourBookingService.getTourBookingDetailById(bookingId);
            return ResponseEntity.ok(bookingDetail);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
