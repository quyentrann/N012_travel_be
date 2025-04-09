package vn.edu.iuh.fit.tourmanagement.repositories;


import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository

public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {
    Optional<Tour> findById(Long id);

    @EntityGraph(attributePaths = "reviews")  // This ensures reviews are fetched along with tours
    List<Tour> findAll();

    @Query("SELECT t FROM TourDetail t WHERE t.tour.tourId = :tourId")
    List<TourDetail> findTourDetailsByTourId(@Param("tourId") Long tourId);
    
     boolean existsByTourcategory(TourCategory category);

//    lấy danh sách các tour có điểm tương đồng dựa trên lịch sử mà user tìm kiêếm
    @Cacheable(value = "similarTours", key = "#tourIds.hashCode()")
    @Query("SELECT t FROM Tour t WHERE t.tourcategory IN (SELECT DISTINCT t2.tourcategory FROM Tour t2 WHERE t2.tourId IN :tourIds)")
    List<Tour> findSimilarToursBasedOnUserHistory(List<Long> tourIds);

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

//    // Ví dụ tìm tour theo giá
//    List<Tour> findByPriceLessThanEqual(Double price);
//
//    // Tìm tour theo địa điểm
//    List<Tour> findByLocationContaining(String location);
//
//    // Tìm tour phổ biến (giả sử tour phổ biến có rating > 4.0)
//    @Query("SELECT t FROM Tour t JOIN t.reviews r WHERE r.rating > 4.0")
//    List<Tour> findPopularTours();
//
//    // Tìm tour theo ngày khởi hành và tính toán duration từ TourDetail
//    @Query("SELECT t FROM Tour t JOIN t.tourDetails td " +
//            "WHERE td.startDate >= :startDate " +
//            "AND (DATEDIFF(td.endDate, td.startDate) + 1) <= :duration")  // Calculate duration dynamically
//    List<Tour> findByStartDateGreaterThanEqualAndDurationLessThanEqual(
//            @Param("startDate") LocalDate startDate,
//            @Param("duration") Integer duration);
//
//    // Tìm tour theo số lượng chỗ còn lại
//    List<Tour> findByAvailableSlotGreaterThanEqual(Integer availableSlots);
//
//    // Tìm tour theo loại trải nghiệm
//    List<Tour> findByExperiencesContaining(String experienceType);
@Query("SELECT t FROM Tour t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
List<Tour> searchByKeyword(@Param("keyword") String keyword);

}
