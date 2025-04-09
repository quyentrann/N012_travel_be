package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;

import java.util.List;

import java.util.Optional;

@Repository
public interface TourDetailRepository extends JpaRepository<TourDetail, Long> {
    @Query("SELECT td FROM TourDetail td WHERE td.tour.tourId = :tourId")
    List<TourDetail> findTourDetailsByTourId(@Param("tourId") Long tourId);

//    Optional<TourDetail> findTourDetailByTour(Long id);
}


