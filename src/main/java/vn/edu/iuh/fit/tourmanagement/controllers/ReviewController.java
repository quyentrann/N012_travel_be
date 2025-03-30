package vn.edu.iuh.fit.tourmanagement.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Review> createReview(@RequestParam Long bookingId,
                                               @RequestParam byte rating,
                                               @RequestParam String comment) {
        Review newReview = reviewService.createReview(bookingId, rating, comment);
        return ResponseEntity.ok(newReview);
    }

    @GetMapping("/reviews/by-tour/{tourId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByTour(@PathVariable Long tourId) {
        List<ReviewDTO> reviews = reviewRepository.findByTour_TourId(tourId)
                .stream()
                .map(review -> new ReviewDTO(
                        review.getReviewId(),
                        review.getComment(),
                        review.getRating(),
                        review.getReviewDate()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviews);
    }

}
