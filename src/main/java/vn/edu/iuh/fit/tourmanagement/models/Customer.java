package vn.edu.iuh.fit.tourmanagement.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "customer")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "user")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;


    @Column(name = "dob")
    private LocalDate dob;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore //fix book
    private User user;

    @Column(name = "address")
    private String address;

    @Column(name = "gender")
    private boolean gender;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;


    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
//    @JsonManagedReference
    @JsonIgnore
    private List<Review> reviews;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonBackReference
    @JsonIgnore
    private List<TourBooking> bookings;
}
