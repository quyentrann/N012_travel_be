package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.id.TourFavouriteId;
import vn.edu.iuh.fit.tourmanagement.models.TourFavourite;

public interface TourFabouriteRepository extends JpaRepository<TourFavourite, TourFavouriteId> {
}
