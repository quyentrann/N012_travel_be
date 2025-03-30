package vn.edu.iuh.fit.tourmanagement.models;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Entity
@Table(name = "review")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "tour")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false) // Liên kết với Tour
//    @JsonBackReference
    @JsonIgnore
    private Tour tour;

    @ManyToOne
    @JoinColumn(name = "customer_id")
//    @JsonBackReference
    @JsonIgnore
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private TourBooking booking;

    @Column(name = "rating")
    private byte rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "review_date")
    private LocalDate reviewDate;
}
