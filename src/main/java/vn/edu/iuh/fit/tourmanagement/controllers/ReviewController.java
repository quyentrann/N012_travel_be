package vn.edu.iuh.fit.tourmanagement.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.ReviewDTO;
import vn.edu.iuh.fit.tourmanagement.dto.ReviewRequest;
import vn.edu.iuh.fit.tourmanagement.models.Review;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.repositories.ReviewRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;
import vn.edu.iuh.fit.tourmanagement.services.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final TourBookingRepository bookingRepository;

    @PostMapping
    public ResponseEntity<?> submitReview(
            @RequestBody ReviewRequest reviewRequest,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập để đánh giá!");
        }

        try {
            ReviewDTO reviewDTO = reviewService.submitReview(
                    reviewRequest.getBookingId(),
                    reviewRequest.getRating(),
                    reviewRequest.getComment(),
                    authentication
            );
            return ResponseEntity.ok(reviewDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-tour/{tourId}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByTour(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByTour_TourId(tourId, pageable);
        Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::new);
        return ResponseEntity.ok(reviewDTOs);
    }

    @GetMapping("/by-booking/{bookingId}")
    public ResponseEntity<Boolean> checkReviewExists(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        if (!booking.getCustomer().getUser().getUsername().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }

        boolean exists = reviewRepository.existsByBooking(booking);
        return ResponseEntity.ok(exists);
    }
}