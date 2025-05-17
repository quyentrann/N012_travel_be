package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourFavouriteRequestDTO {
    private Long customerId;
    private Long tourId;
}
