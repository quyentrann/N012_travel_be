package vn.edu.iuh.fit.tourmanagement.models;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;


@Entity
@Table(name = "tour_favourite")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@IdClass(TourFavouriteId.class)
public class TourFavourite {
    @Id
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Id
    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;
}

@Data
class TourFavouriteId implements Serializable {
    private Customer customer;
    private Tour tour;
}
