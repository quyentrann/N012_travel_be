package vn.edu.iuh.fit.tourmanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.dto.ReviewDTO;
import vn.edu.iuh.fit.tourmanagement.models.Review;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.repositories.ReviewRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TourBookingRepository bookingRepository;

    public ReviewDTO createReview(Long bookingId, byte rating, String comment) {
        // Lấy booking từ database
        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking!"));

        // Tạo review mới
        Review review = new Review();
        review.setTour(booking.getTour());  // Lưu tour_id từ booking
        review.setBooking(booking);
        review.setCustomer(booking.getCustomer());
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewDate(LocalDate.now());

        // Lưu vào database
        Review savedReview = reviewRepository.save(review);

        // Tạo ReviewDTO trả về bao gồm tên khách hàng
        return ReviewDTO.builder()
                .reviewId(savedReview.getReviewId())
                .comment(savedReview.getComment())
                .rating(savedReview.getRating())
                .reviewDate(savedReview.getReviewDate())
                .customerFullName(savedReview.getCustomer().getFullName()) // Thêm tên khách hàng vào DTO
                .build();
    }
}
