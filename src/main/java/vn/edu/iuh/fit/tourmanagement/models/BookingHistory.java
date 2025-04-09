package vn.edu.iuh.fit.tourmanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private TourBooking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus newStatus;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate = LocalDateTime.now();

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "cancellation_fee")
    private double cancellationFee; // Thêm phí hủy

    @Column(name = "refund_amount")
    private double refundAmount; // Thêm số tiền hoàn lại
}
