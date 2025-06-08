package vn.edu.iuh.fit.tourmanagement.controllers;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.edu.iuh.fit.tourmanagement.dto.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.enums.RefundStatus;
import vn.edu.iuh.fit.tourmanagement.models.BookingHistory;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.BookingHistoryRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;
import vn.edu.iuh.fit.tourmanagement.services.TourBookingService;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/bookings")
public class TourBookingController {
    private static final Logger logger = LoggerFactory.getLogger(TourBookingController.class);
    @Autowired
    private TourBookingService tourBookingService;

    @Autowired
    private TourService tourService;
    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private BookingHistoryRepository bookingHistoryRepository;
    @GetMapping
    public Object getAllTourBooking() {
        return tourBookingService.getListTourBooking();
    }

    private double calculateCancellationFee(TourBooking booking, LocalDateTime cancelDate, boolean isHoliday) {
        LocalDateTime tourStartDate = booking.getDepartureDate().atStartOfDay();
        LocalDateTime bookingDate = booking.getBookingDate();
        long daysBeforeTour = java.time.temporal.ChronoUnit.DAYS.between(cancelDate, tourStartDate);
        long hoursSinceBooking = java.time.temporal.ChronoUnit.HOURS.between(bookingDate, cancelDate);

        // Miễn phí hủy trong 24 giờ sau khi đặt nếu ngày khởi hành còn >= 7 ngày
        if (hoursSinceBooking <= 24 && daysBeforeTour >= 7) {
            return 0.0; // Hoàn tiền 100%
        }

        double cancellationFee = 0;
        if (isHoliday) {
            if (daysBeforeTour >= 30) {
                cancellationFee = 0.2 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 15) {
                cancellationFee = 0.4 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                cancellationFee = 0.6 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 3) {
                cancellationFee = 0.8 * booking.getTotalPrice();
            } else {
                cancellationFee = booking.getTotalPrice();
            }
        } else {
            if (daysBeforeTour >= 14) {
                cancellationFee = 0.1 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                cancellationFee = 0.3 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 4) {
                cancellationFee = 0.5 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 1) {
                cancellationFee = 0.7 * booking.getTotalPrice();
            } else {
                cancellationFee = booking.getTotalPrice();
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
                tourBooking.getNumberAdults(),   // Thêm numberAdults
                tourBooking.getNumberChildren(), // Thêm numberChildren
                tourBooking.getNumberInfants(),  // Thêm numberInfants
                tourBooking.getTotalPrice(),
                tourBooking.getBookingDate(),
                tourBooking.getDepartureDate(), // Thêm departureDate
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
                    booking.getNumberAdults(),   // Thêm numberAdults
                    booking.getNumberChildren(), // Thêm numberChildren
                    booking.getNumberInfants(),  // Thêm numberInfants
                    booking.getTotalPrice(),
                    booking.getBookingDate(),
                    booking.getDepartureDate(),
                    booking.getStatus().toString(),
                    tourDTO
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTOs);
    }

    // TourBookingController.java
    @GetMapping("/history/{bookingId}/entries")
    public ResponseEntity<List<BookingHistoryDTO>> getBookingHistoryEntries(
            @PathVariable Long bookingId, Authentication authentication) {
        logger.info("Request to get booking history for bookingId: {}, user: {}", bookingId, authentication.getName());

        if (!(authentication.getPrincipal() instanceof User)) {
            logger.warn("Unauthorized access: principal is not a User");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            logger.warn("Forbidden: user {} has no customer profile", user.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        TourBooking booking = tourBookingService.getTourBookingById(bookingId);
        if (booking == null || !booking.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            logger.warn("Booking {} not found or user {} does not have permission", bookingId, user.getUsername());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<BookingHistory> histories = tourBookingService.getBookingHistory(bookingId);
        List<BookingHistoryDTO> historyDTOs = histories.stream().map(history -> new BookingHistoryDTO(
                history.getId(),
                history.getBooking().getBookingId(),
                history.getOldStatus().toString(),
                history.getNewStatus().toString(),
                history.getChangeDate(),
                history.getReason(),
                history.getCancellationFee(),
                history.getRefundAmount(),
                history.getAdditionalPayment(),
                history.getRefundStatus().toString(),
                history.getCancelDate(),
                history.isHoliday()
        )).collect(Collectors.toList());

        logger.info("Successfully retrieved {} history entries for bookingId: {}", historyDTOs.size(), bookingId);
        return ResponseEntity.ok(historyDTOs);
    }

    @GetMapping("/pending-payment")
    public ResponseEntity<List<TourBookingDTO>> getPendingPaymentBookings(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        LocalDateTime now = LocalDateTime.now();
        List<TourBooking> bookings = tourBookingRepository.findByCustomerCustomerIdAndStatus(user.getCustomer().getCustomerId(), BookingStatus.CONFIRMED)
                .stream()
                .filter(booking -> now.isBefore(booking.getBookingDate().plusDays(3)))
                .collect(Collectors.toList());

        List<TourBookingDTO> bookingDTOs = bookings.stream().map(booking -> {
            Tour tour = booking.getTour();
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
                    Collections.emptyList() // Tránh fetch bookings để tối ưu hiệu suất
            );
            return new TourBookingDTO(
                    booking.getBookingId(),
                    booking.getNumberPeople(),
                    booking.getNumberAdults(),   // Thêm numberAdults
                    booking.getNumberChildren(), // Thêm numberChildren
                    booking.getNumberInfants(),  // Thêm numberInfants
                    booking.getTotalPrice(),
                    booking.getBookingDate(),
                    booking.getDepartureDate(),
                    booking.getStatus().toString(),
                    tourDTO
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(bookingDTOs);
    }

    @PostMapping("/calculate-change-fee/{bookingId}")
    public ResponseEntity<ChangeTourResponse> calculateChangeFee(
            @PathVariable Long bookingId,
            @RequestBody ChangeTourRequest request,
            Authentication authentication
    ) {
        if (!(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            ChangeTourResponse response = tourBookingService.calculateChangeFee(bookingId, request, user);
            long hoursSinceBooking = java.time.temporal.ChronoUnit.HOURS.between(
                    tourBookingRepository.findById(bookingId).get().getBookingDate(),
                    LocalDateTime.now()
            );
            if (hoursSinceBooking <= 24 && response.getChangeFee() == 0) {
                response.setMessage("Miễn phí đổi tour trong 24 giờ sau khi đặt!");
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ChangeTourResponse() {{
                setMessage("Lỗi: " + e.getMessage());
            }});
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ChangeTourResponse() {{
                setMessage("Lỗi: " + e.getMessage());
            }});
        }
    }

    @PutMapping("/change/{bookingId}")
    public ResponseEntity<?> changeTour(
            @PathVariable Long bookingId,
            @RequestBody ChangeTourRequest request,
            Authentication authentication
    ) {
        if (!(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            TourBooking updatedBooking = tourBookingService.changeTour(bookingId, request, user);
            return ResponseEntity.ok(updatedBooking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // TourBookingController.java
    @PostMapping("/confirm-additional-payment/{bookingId}")
    public ResponseEntity<?> confirmAdditionalPayment(
            @PathVariable Long bookingId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking không tồn tại"));
        if (!booking.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Bạn không có quyền xác nhận thanh toán này"));
        }

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Booking không ở trạng thái PENDING_PAYMENT"));
        }

        booking.setStatus(BookingStatus.PAID);
        tourBookingRepository.save(booking);

        // Log the status change in BookingHistory
        BookingHistory history = BookingHistory.builder()
                .booking(booking)
                .oldStatus(BookingStatus.PENDING_PAYMENT)
                .newStatus(BookingStatus.PAID)
                .changeDate(LocalDateTime.now())
                .reason("Xác nhận thanh toán bổ sung thành công")
                .cancellationFee(0)
                .refundAmount(0)
                .additionalPayment(0)
                .refundStatus(RefundStatus.NONE)
                .tour(booking.getTour())
                .isHoliday(false)
                .cancelDate(null)
                .build();
        bookingHistoryRepository.save(history);
        logger.info("BookingHistory saved for additional payment confirmation: bookingId={}, newStatus=PAID", bookingId);

        return ResponseEntity.ok(Collections.singletonMap("message", "Thanh toán bổ sung đã được xác nhận"));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<TourBooking> getBookingById(@PathVariable Long id) {
        Optional<TourBooking> booking = tourBookingService.getBookingById(id);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

}
