package vn.edu.iuh.fit.tourmanagement.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TourBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    @JsonBackReference
    private Tour tour;

    @Column(name = "number_people")
    private int numberPeople;

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @Column(name = "departure_date")
    private LocalDate departureDate; // Thêm trường này

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @Column(name = "number_adults", nullable = false)
    private int numberAdults;

    @Column(name = "number_children", nullable = false)
    private int numberChildren;

    @Column(name = "number_infants", nullable = false)
    private int numberInfants;

    @Column(name = "reminder_sent") // Thêm trường mới
    private boolean reminderSent = false;
}