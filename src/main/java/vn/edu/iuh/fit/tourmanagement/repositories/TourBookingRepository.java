package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {
}
