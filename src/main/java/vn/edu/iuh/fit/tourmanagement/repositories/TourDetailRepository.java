package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;

public interface TourDetailRepository extends JpaRepository<TourDetail, Long> {
}
