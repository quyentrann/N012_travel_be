package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.id.TourFavouriteId;
import vn.edu.iuh.fit.tourmanagement.models.TourFavourite;

import java.util.List;

public interface TourFabouriteRepository extends JpaRepository<TourFavourite, TourFavouriteId> {
    boolean existsByCustomer_CustomerIdAndTour_TourId(Long customerId, Long tourId);
    List<TourFavourite> findByCustomer_CustomerId(Long customerId);
    void deleteByCustomer_CustomerIdAndTour_TourId(Long customerId, Long tourId);
}
