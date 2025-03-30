package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;

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
}
