package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.tourmanagement.models.BookingHistory;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {

    @Query("SELECT tb FROM TourBooking tb WHERE tb.customer.customerId = :customerId")
    List<TourBooking> findByCustomerId(@Param("customerId") Long customerId);

    Optional<TourBooking> findById(Long id);

//    List<BookingHistory> findByTour_TourId(Long tourId);


}
