package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.tourmanagement.models.BookingHistory;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

import java.util.List;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {
    List<BookingHistory> findByTour_TourId(Long tourId);

    @Query("SELECT b FROM TourBooking b JOIN FETCH b.tour WHERE b.customer.customerId = :customerId")
    List<TourBooking> getBookingHistoryByCustomerId(@Param("customerId") Long customerId);

}