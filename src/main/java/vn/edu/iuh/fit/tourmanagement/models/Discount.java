package vn.edu.iuh.fit.tourmanagement.models;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.tourmanagement.enums.DiscountStype;

import java.time.LocalDate;

@Entity
@Table(name = "discount")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Long discountId;

    @Column(name = "code", length = 255)
    private String code;
    @Column(name = "description")
    private String description;
    @Column(name = "discount_percent")
    private double discountPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_stype")
    private DiscountStype discountStype;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "quantity")
    private int quantity;
    @Column(name = "min_order_value")
    private double minOrderValue;
}
