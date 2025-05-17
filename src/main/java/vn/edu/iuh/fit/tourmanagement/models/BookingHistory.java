package vn.edu.iuh.fit.tourmanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.enums.RefundStatus;

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

    @Column(name = "additional_payment")
    private double additionalPayment; // Số tiền cần thanh toán bổ sung

    @Column(name = "refund_status")
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus; // Trạng thái hoàn tiền

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus newStatus;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate = LocalDateTime.now();

    @Column(name = "cancel_date")
    private LocalDateTime cancelDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;


    @Column(name = "cancellation_fee")
    private double cancellationFee; // Thêm phí hủy

    @Column(name = "refund_amount")
    private double refundAmount; // Thêm số tiền hoàn lại

    @Column(name = "is_holiday")
    private boolean isHoliday;

}
