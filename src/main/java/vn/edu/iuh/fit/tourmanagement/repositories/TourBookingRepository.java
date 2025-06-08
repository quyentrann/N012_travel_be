package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.models.BookingHistory;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TourBookingRepository extends JpaRepository<TourBooking, Long> {

    @Query("SELECT tb FROM TourBooking tb WHERE tb.customer.customerId = :customerId")
    List<TourBooking> findByCustomerId(@Param("customerId") Long customerId);

    Optional<TourBooking> findById(Long id);

    @Query("SELECT tb FROM TourBooking tb WHERE tb.tour.tourId = :tourId")
    List<TourBooking> findByTourTourId(@Param("tourId") Long tourId);

//    List<BookingHistory> findByTour_TourId(Long tourId);
List<TourBooking> findByStatus(BookingStatus status);

    List<TourBooking> findByCustomerCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM TourBooking b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    long countBookingsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<TourBooking> findByBookingDateBetweenAndStatusNot(LocalDateTime startDate, LocalDateTime endDate, BookingStatus status);

    // Truy vấn các đơn bị hủy trong khoảng thời gian
    List<TourBooking> findByBookingDateBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, BookingStatus status);

    @Query("SELECT t FROM TourBooking t WHERE t.bookingDate BETWEEN :startDate AND :endDate ORDER BY t.bookingDate DESC")
    List<TourBooking> findBookingsByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.tour.tourId, b.tour.name, SUM(b.numberPeople) AS totalPeople " +
            "FROM TourBooking b " +
            "WHERE b.bookingDate BETWEEN :start AND :end " +
            "GROUP BY b.tour.tourId, b.tour.name " +
            "ORDER BY totalPeople DESC")
    List<Object[]> findTopToursByTotalPeople(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             Pageable pageable);

    @Query("SELECT DATE(b.bookingDate) AS bookingDate, COUNT(b) AS count " +
            "FROM TourBooking b " +
            "WHERE b.tour.tourId = :tourId AND b.bookingDate BETWEEN :start AND :end " +
            "GROUP BY DATE(b.bookingDate) " +
            "ORDER BY DATE(b.bookingDate)")
    List<Object[]> findBookingCountsByTourAndDate(
            @Param("tourId") Long tourId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Truy vấn top tour với tổng số lượt đặt theo ngày
    @Query("SELECT b.tour.tourId AS tourId, b.tour.name AS tourName, DATE(b.bookingDate) AS bookingDate, COUNT(b) AS count " +
            "FROM TourBooking b " +
            "WHERE b.bookingDate BETWEEN :start AND :end " +
            "GROUP BY b.tour.tourId, b.tour.name, DATE(b.bookingDate) " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> findTopToursWithBookingCounts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT b FROM TourBooking b WHERE b.status IN :statuses AND b.bookingDate BETWEEN :start AND :end")
    List<TourBooking> findByDateRangeAndStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<BookingStatus> statuses
    );

    List<TourBooking> findByBookingDateBetweenAndStatusIn(LocalDateTime startDate, LocalDateTime endDate, List<BookingStatus> status);
    @Query("SELECT DISTINCT tb.tour.tourId FROM TourBooking tb WHERE tb.bookingDate BETWEEN :startDate AND :endDate")
    List<Long> findDistinctTourIdsByBookingDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT COUNT(tb)\n" +
            "    FROM TourBooking tb\n" +
            "    WHERE tb.bookingDate BETWEEN :startDate AND :endDate\n" +
            "      AND tb.tour.tourId = :tourId AND tb.status <> 'CANCELED' AND tb.status <> 'CONFIRMED' ")
    int countByBookingDateBetweenAndTour_TourId(LocalDateTime startDate, LocalDateTime endDate, Long tourId);
    @Query("SELECT SUM(tb.totalPrice) FROM TourBooking tb WHERE tb.bookingDate BETWEEN :startDate AND :endDate AND tb.tour.tourId = :tourId AND tb.status <> 'CANCELED' AND tb.status <> 'CONFIRMED' ")
    Double getTotalRevenueByDateAndTourId(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("tourId") Long tourId
    );

    List<TourBooking> findByTour_TourIdAndDepartureDate(Long tourId, LocalDate departureDate);
}
