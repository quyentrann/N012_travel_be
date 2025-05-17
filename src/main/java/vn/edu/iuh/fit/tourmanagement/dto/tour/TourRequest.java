package vn.edu.iuh.fit.tourmanagement.dto.tour;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourRequest {
    private String name;
    private String location;
    private int price;
    private int availableSlot;
    private Long tourcategoryId;  // Giữ categoryId
    private String description;
    private String status;
    private String imageURL;
}
