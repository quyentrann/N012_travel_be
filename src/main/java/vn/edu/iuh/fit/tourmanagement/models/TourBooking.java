package vn.edu.iuh.fit.tourmanagement.models;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;

import java.time.LocalDate;
@Entity
@Table(name = "booking")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TourBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "number_people")
    private int numberPeople;

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "status")
    private BookingStatus status;
}
