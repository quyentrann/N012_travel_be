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
    private byte rating;
    private LocalDate reviewDate;
    private String customerFullName; // Thêm trường để chứa tên khách hàng
    private String avatarUrl; // Thêm avatar

    public ReviewDTO(Review review) {
        if (review != null) {
            this.reviewId = review.getReviewId();
            this.comment = review.getComment();
            this.rating = review.getRating();
            this.reviewDate = review.getReviewDate();
        }
    }

}
