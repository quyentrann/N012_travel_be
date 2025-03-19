package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

import java.util.List;

public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {

    @Query("SELECT tb FROM TourBooking tb WHERE tb.customer.customerId = :customerId")
    List<TourBooking> findByCustomerId(@Param("customerId") Long customerId);

}
