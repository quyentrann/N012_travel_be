package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;

public interface TourCategoryRepository extends JpaRepository<TourCategory, Long> {
}
