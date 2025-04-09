package vn.edu.iuh.fit.tourmanagement.controllers;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.ReviewDTO;
import vn.edu.iuh.fit.tourmanagement.models.Review;
import vn.edu.iuh.fit.tourmanagement.repositories.ReviewRepository;
import vn.edu.iuh.fit.tourmanagement.services.ReviewService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @PostMapping("/create")
    public ResponseEntity<ReviewDTO> createReview(@RequestParam Long bookingId,
                                                  @RequestParam byte rating,
                                                  @RequestParam String comment) {
        ReviewDTO newReviewDTO = reviewService.createReview(bookingId, rating, comment);
        return ResponseEntity.ok(newReviewDTO);
    }

    @GetMapping("/reviews/by-tour/{tourId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByTour(@PathVariable Long tourId) {
        List<ReviewDTO> reviews = reviewRepository.findByTour_TourId(tourId)
                .stream()
                .map(review -> new ReviewDTO(
                        review.getReviewId(),
                        review.getComment(),
                        review.getRating(),
                        review.getReviewDate(),
                        review.getCustomer() != null ? review.getCustomer().getFullName() : "N/A", // Kiểm tra null
                        review.getCustomer() != null ? review.getCustomer().getAvatarUrl() : null // Kiểm tra null
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviews);
    }


    @PostMapping("/submit")
    public ResponseEntity<?> submitReview(@RequestParam Long bookingId,
                                          @RequestParam byte rating,
                                          @RequestParam String comment,
                                          Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập để đánh giá!");
        }

        try {
            ReviewDTO reviewDTO = reviewService.submitReview(bookingId, rating, comment, authentication);
            return ResponseEntity.ok(reviewDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
