package vn.edu.iuh.fit.tourmanagement.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;

import java.util.List;

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
    @Enumerated(EnumType.STRING)
    private TourStatus status;

    @Column(name = "image_url")
    private String imageURL;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TourDetail> tourDetails;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<TourSchedule> tourSchedules;

}
