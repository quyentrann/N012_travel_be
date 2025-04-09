package vn.edu.iuh.fit.tourmanagement.models;

import jakarta.persistence.*;
import lombok.*;

//import java.security.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "serch_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "search_query",length = 255)
    private String searchQuery;

    @ManyToOne
    @JoinColumn(name = "tour_id",nullable = true)
    private  Tour tour;

    @Column(name = "search_time")
    private LocalDateTime searchTime;

    @Column(name = "click_count")
    private Integer clickCount;
}
