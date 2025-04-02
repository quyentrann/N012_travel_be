package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;
import vn.edu.iuh.fit.tourmanagement.models.TourSchedule;

import java.util.List;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.tourId = :tourId")
    List<TourSchedule> findTourSchedulesByTourId(@Param("tourId") Long tourId);
}
