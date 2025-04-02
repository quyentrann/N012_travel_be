package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.dto.TourBookingRequest;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.models.*;
import vn.edu.iuh.fit.tourmanagement.repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TourBookingService {
    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private BookingHistoryRepository bookingHistoryRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public List<TourBooking> getListTourBooking() {
        return tourBookingRepository.findAll();
    }

    public List<TourBooking> getTourBookingByCustomerId(Long customerId) {
        return tourBookingRepository.findByCustomerId(customerId);
    }

    public TourBooking getTourBookingById(Long id) {
        return tourBookingRepository.findById(id).orElse(null);
    }

    public TourBooking bookTour(TourBookingRequest bookingRequest, Authentication authentication) throws Exception {
        // Lấy thông tin User từ Authentication
        User user = (User) authentication.getPrincipal();

        // Kiểm tra xem user có customer không
        if (user.getCustomer() == null) {
            throw new Exception("User is not a customer");
        }

        // Lấy Customer từ User
        Customer customer = user.getCustomer();

        // Lấy thông tin tour
        Tour tour = tourRepository.findById(bookingRequest.getTourId())
                .orElseThrow(() -> new Exception("Tour not found"));

        // Kiểm tra số lượng slot
        if (tour.getAvailableSlot() < bookingRequest.getNumberPeople()) {
            throw new Exception("Not enough available slots");
        }

        // Tính tổng giá
        double totalPrice = bookingRequest.getTotalPrice();

        // Tạo đối tượng TourBooking
        TourBooking booking = TourBooking.builder()
                .customer(customer)
                .tour(tour)
                .numberPeople(bookingRequest.getNumberPeople())
                .totalPrice(totalPrice)
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        // Lưu thông tin booking vào DB
        TourBooking savedBooking = tourBookingRepository.save(booking);

        // Cập nhật số lượng slot còn lại của tour
        tour.setAvailableSlot(tour.getAvailableSlot() - bookingRequest.getNumberPeople());
        tourRepository.save(tour);

        return savedBooking;
    }



    @Transactional
    public void cancelBooking(Long bookingId, String reason) {
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy booking"));

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalStateException("Booking đã bị hủy trước đó");
        }

        // Ghi nhận lịch sử hủy
        bookingHistoryRepository.save(BookingHistory.builder()
                .booking(booking)
                .oldStatus(booking.getStatus())
                .newStatus(BookingStatus.CANCELED)
                .changeDate(LocalDateTime.now())
                .reason(reason)
                .build());

        // Cập nhật trạng thái booking
        booking.setStatus(BookingStatus.CANCELED);
        tourBookingRepository.save(booking);
    }

    public List<BookingHistory> getBookingHistory(Long bookingId) {
        return bookingHistoryRepository.findByTour_TourId(bookingId);
    }
}
