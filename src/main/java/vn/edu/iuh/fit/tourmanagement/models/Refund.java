package vn.edu.iuh.fit.tourmanagement.models;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "refund")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(name = "refund_date")
    private LocalDate refundDate;

    @Column(name = "reason")
    private String reason;
}
