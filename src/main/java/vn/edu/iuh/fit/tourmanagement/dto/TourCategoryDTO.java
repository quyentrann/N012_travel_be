package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourCategoryDTO {
    private Long categoryId;
    private String categoryName;
    private String description;

}
