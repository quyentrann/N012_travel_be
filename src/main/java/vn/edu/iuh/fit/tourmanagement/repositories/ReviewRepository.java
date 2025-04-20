package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.Review;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

import java.util.List;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
//    List<Review> findByTour_TourId(Long tourId);

    Page<Review> findByTour_TourId(Long tourId, Pageable pageable);
    boolean existsByBooking(TourBooking booking);

}
