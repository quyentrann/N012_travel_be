package vn.edu.iuh.fit.tourmanagement.models;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;

@Entity
@Table(name = "tour")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id")
    private Long tourId;

    @ManyToOne
    @JoinColumn(name = "tour_category_id")
    private TourCategory tourcategory;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "price")
    private double price;

    @Column(name = "available_slot")
    private int availableSlot;

    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private TourStatus status;

    @Column(name = "image_url")
    private String imageURL;
}
