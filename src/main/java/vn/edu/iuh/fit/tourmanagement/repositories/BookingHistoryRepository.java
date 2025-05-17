package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.tourmanagement.models.BookingHistory;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;

import java.util.List;
import java.util.Optional;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {
    List<BookingHistory> findByTour_TourId(Long tourId);

    @Query("SELECT b FROM TourBooking b JOIN FETCH b.tour WHERE b.customer.customerId = :customerId")
    List<TourBooking> getBookingHistoryByCustomerId(@Param("customerId") Long customerId);

    List<BookingHistory> findByBooking_BookingId(Long bookingId);

    @Query("SELECT bh FROM BookingHistory bh WHERE bh.booking.bookingId = :bookingId AND bh.reason LIKE %:reason%")
    Optional<BookingHistory> findByBooking_BookingIdAndReasonContaining(
            @Param("bookingId") Long bookingId,
            @Param("reason") String reason);
}