package vn.edu.iuh.fit.tourmanagement.services;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.dto.ReviewDTO;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
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

//public ReviewDTO createReview(Long bookingId, byte rating, String comment) {
//        // Lấy booking từ database
//        TourBooking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking!"));
//
//        // Tạo review mới
//        Review review = new Review();
//        review.setTour(booking.getTour());  // Lưu tour_id từ booking
//        review.setBooking(booking);
//        review.setCustomer(booking.getCustomer());
//        review.setRating(rating);
//        review.setComment(comment);
//        review.setReviewDate(LocalDate.now());
//
//        // Lưu vào database
//        Review savedReview = reviewRepository.save(review);
//
//        // Tạo ReviewDTO trả về bao gồm tên khách hàng
//        return ReviewDTO.builder()
//                .reviewId(savedReview.getReviewId())
//                .comment(savedReview.getComment())
//                .rating(savedReview.getRating())
//                .reviewDate(savedReview.getReviewDate())
//                .customerFullName(savedReview.getCustomer().getFullName()) // Thêm tên khách hàng vào DTO
//                .build();
//    }

    @Transactional
    public ReviewDTO submitReview(Long bookingId, float rating, String comment, Authentication authentication) {
        // Validation thủ công
        if (bookingId == null) {
            throw new RuntimeException("Booking ID không được để trống!");
        }
        if (rating < 0 || rating > 5) {
            throw new RuntimeException("Điểm đánh giá phải từ 0 đến 5!");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new RuntimeException("Nhận xét không được để trống!");
        }
        if (comment.length() > 1000) {
            throw new RuntimeException("Nhận xét không được vượt quá 1000 ký tự!");
        }

        TourBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại!"));


        // Kiểm tra trạng thái booking
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể đánh giá booking đã hoàn thành!");
        }

        // Kiểm tra quyền
        String username = authentication.getName();
        Customer customer = booking.getCustomer();
        if (!customer.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền đánh giá booking này!");
        }

        // Kiểm tra đánh giá trùng lặp
        if (reviewRepository.existsByBooking(booking)) {
            throw new RuntimeException("Bạn đã đánh giá tour này rồi!");
        }

        // Tạo và lưu đánh giá
        Review review = Review.builder()
                .booking(booking)
                .customer(customer)
                .tour(booking.getTour())
                .rating(rating)
                .comment(comment)
                .reviewDate(LocalDate.now())
                .build();

        reviewRepository.save(review);

        return new ReviewDTO(review);
    }


}
