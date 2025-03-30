package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;

import java.util.List;

@Repository
public interface TourDetailRepository extends JpaRepository<TourDetail, Long> {
}