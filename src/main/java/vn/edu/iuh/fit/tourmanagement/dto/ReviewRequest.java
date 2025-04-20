package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {
    private Long bookingId;
    private float rating;
    private String comment;
}