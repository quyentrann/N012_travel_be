package vn.edu.iuh.fit.tourmanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.dto.ReviewDTO;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
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

    public ReviewDTO submitReview(Long bookingId, byte rating, String comment, Authentication authentication) {
        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));

        String username = authentication.getName(); // Lấy username của user đang đăng nhập
        Customer customer = booking.getCustomer();

        if (!customer.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền đánh giá booking này!");
        }

        if (reviewRepository.existsByBooking(booking)) {
            throw new RuntimeException("Bạn đã đánh giá tour này rồi!");
        }

        Review review = Review.builder()
                .booking(booking)
                .customer(customer)
                .tour(booking.getTour())
                .rating(rating)
                .comment(comment)
                .reviewDate(LocalDate.now())
                .build();

        reviewRepository.save(review);

        return new ReviewDTO(review.getReviewId(), review.getComment(), review.getRating(),
                review.getReviewDate(), customer.getFullName(), customer.getAvatarUrl());
    }


}
