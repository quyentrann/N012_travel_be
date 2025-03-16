package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.tourmanagement.models.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
}
