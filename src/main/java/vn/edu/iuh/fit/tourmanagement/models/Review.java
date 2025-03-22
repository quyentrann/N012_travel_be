package vn.edu.iuh.fit.tourmanagement.models;
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
@ToString
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
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
