package vn.edu.iuh.fit.tourmanagement.dto.tour;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourDTO {
    private Long tourId;
    private String name;
    private String location;
    private double price;
    private int availableSlot;
    private TourCategory category;  // Giữ categoryId
    private String description;
    private TourStatus status;
    private String imageURL;

}
