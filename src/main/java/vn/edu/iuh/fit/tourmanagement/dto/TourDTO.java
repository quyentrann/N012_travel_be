package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourDTO {
    private Long tourId;
    private String name;
    private Double price;
    private int availableSlot;
    private String location;
    private String description;
    private String highlights;
    private String imageURL;
    private String experiences;
    private String status;
    private TourCategoryDTO tourCategory;
    private List<TourDetailDTO> tourDetails;
    private List<TourScheduleDTO> tourSchedules;
    private List<ReviewDTO> reviews;
    private List<BookingDTO> bookings;
}