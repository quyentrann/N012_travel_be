package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    Optional<Tour> findById(Long id);

    @Query("SELECT t FROM TourDetail t WHERE t.tour.tourId = :tourId")
    List<TourDetail> findTourDetailsByTourId(@Param("tourId") Long tourId);


    boolean existsByTourcategory(TourCategory category);

//    lấy danh sách các tour có điểm tương đồng dựa trên lịch sử mà user tìm kiêếm
    @Cacheable(value = "similarTours", key = "#tourIds.hashCode()")
    @Query("SELECT t FROM Tour t WHERE t.tourcategory IN (SELECT DISTINCT t2.tourcategory FROM Tour t2 WHERE t2.tourId IN :tourIds)")
    List<Tour> findSimilarToursBasedOnUserHistory(List<Long> tourIds);

}
