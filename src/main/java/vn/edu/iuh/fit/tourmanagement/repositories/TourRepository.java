package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    Optional<Tour> findById(Long id);

    @EntityGraph(attributePaths = "reviews")  // This ensures reviews are fetched along with tours
    List<Tour> findAll();

    @Query("SELECT t FROM TourDetail t WHERE t.tour.tourId = :tourId")
    List<TourDetail> findTourDetailsByTourId(@Param("tourId") Long tourId);

    @Query("SELECT t FROM Tour t " +
            "WHERE t.tourId <> :tourId " +  // Loại bỏ tour hiện tại
            "AND (t.location LIKE %:location% OR t.name LIKE %:name%) " +  // Tìm theo địa điểm hoặc tên
            "ORDER BY CASE " +
            "WHEN t.location LIKE %:location% THEN 1 " +  // Ưu tiên các tour có địa điểm giống với location
            "ELSE 2 " +  // Các tour còn lại
            "END")
    List<Tour> findSimilarTours(
            @Param("tourId") Long tourId,
            @Param("name") String name,
            @Param("location") String location
    );







}
