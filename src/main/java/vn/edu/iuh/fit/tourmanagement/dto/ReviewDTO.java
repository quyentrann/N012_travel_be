package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;
import vn.edu.iuh.fit.tourmanagement.models.Review;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDTO {
    private Long reviewId;
    private String comment;
    private float rating; // Đổi sang float để hỗ trợ nửa sao (0.5, 1.0, 1.5, ...)
    private LocalDate reviewDate;
    private String customerFullName; // Tên đầy đủ của khách hàng
    private String avatarUrl; // URL ảnh đại diện

    public ReviewDTO(Review review) {
        if (review != null) {
            this.reviewId = review.getReviewId();
            this.comment = review.getComment();
            this.rating = review.getRating(); // Ép kiểu từ byte sang float
            this.reviewDate = review.getReviewDate();
            this.customerFullName = review.getCustomer() != null ? review.getCustomer().getFullName() : "Khách ẩn danh";
            this.avatarUrl = review.getCustomer() != null ? review.getCustomer().getAvatarUrl() : null;
        }
    }
}