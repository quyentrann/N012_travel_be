package vn.edu.iuh.fit.tourmanagement.models;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.PaymentMethod;
import vn.edu.iuh.fit.tourmanagement.enums.PaymentStatus;

import java.time.LocalDate;

@Entity
@Table(name = "payment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "tour_booking_id")
    private TourBooking tourBooking;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

}
