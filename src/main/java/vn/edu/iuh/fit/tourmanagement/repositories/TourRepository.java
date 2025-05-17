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

@Query("SELECT t FROM Tour t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
List<Tour> searchByKeyword(@Param("keyword") String keyword);

    List<Tour> findByTourcategory_CategoryId(Long categoryId);
    // Lọc tour theo giá trong khoảng minPrice và maxPrice
    List<Tour> findByPriceBetween(double minPrice, double maxPrice);

    List<Tour> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String desc);
    List<Tour> findByLocationContainingIgnoreCase(String query);

    @Query("SELECT t FROM Tour t JOIN t.tourDetails td WHERE (td.startDate BETWEEN :startDate AND :endDate) OR (td.endDate BETWEEN :startDate AND :endDate)")
    List<Tour> findToursByTimeRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    public List<Tour> findByTourDetailsStartDateOrTourDetailsEndDate(LocalDate startDate, LocalDate endDate);


    public List<Tour> findByTourDetailsStartDateBetweenOrTourDetailsEndDateBetween(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2);


}
