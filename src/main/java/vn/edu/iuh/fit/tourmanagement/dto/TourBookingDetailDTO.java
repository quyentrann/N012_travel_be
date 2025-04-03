package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourBookingDetailDTO {
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

    // Constructor ánh xạ từ TourBooking
    public TourBookingDetailDTO(TourBooking booking) {
        if (booking != null && booking.getTour() != null) {
            this.tourId = booking.getTour().getTourId();
            this.name = booking.getTour().getName();
            this.price = booking.getTour().getPrice();
            this.availableSlot = booking.getTour().getAvailableSlot();
            this.location = booking.getTour().getLocation();
            this.description = booking.getTour().getDescription();
            this.highlights = booking.getTour().getHighlights();
            this.imageURL = booking.getTour().getImageURL();
            this.experiences = booking.getTour().getExperiences();
            this.status = booking.getTour().getStatus().name(); // Enum -> String
            this.tourCategory = new TourCategoryDTO(
                    booking.getTour().getTourcategory().getCategoryId(),
                    booking.getTour().getTourcategory().getCategoryName(),
                    booking.getTour().getTourcategory().getDescription()
            );
            this.tourDetails = booking.getTour().getTourDetails().stream()
                    .map(detail -> new TourDetailDTO(detail))
                    .collect(Collectors.toList());
            this.tourSchedules = booking.getTour().getTourSchedules().stream()
                    .map(schedule -> new TourScheduleDTO(schedule))
                    .collect(Collectors.toList());
            this.reviews = booking.getTour().getReviews().stream()
                    .map(review -> new ReviewDTO(review))
                    .collect(Collectors.toList());
        }
    }
}
