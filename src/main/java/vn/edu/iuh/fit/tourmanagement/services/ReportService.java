package vn.edu.iuh.fit.tourmanagement.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.dto.TourBookingDTO;
import vn.edu.iuh.fit.tourmanagement.dto.report.*;
import vn.edu.iuh.fit.tourmanagement.dto.tourbooking.BookingCountByDate;
import vn.edu.iuh.fit.tourmanagement.dto.tourbooking.TopTourBookingDTO;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.repositories.CustomerRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    @Autowired
    private TourBookingRepository tourBookingRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TourRepository tourRepository;

    public long getTotalBookings(LocalDateTime startDate, LocalDateTime endDate) {
        return tourBookingRepository.countBookingsByDateRange(startDate, endDate);
    }
    public double calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        List<TourBooking> validBookings = tourBookingRepository
                .findByBookingDateBetweenAndStatusNot(startDate, endDate, BookingStatus.CANCELED);
        return validBookings.stream().mapToDouble(TourBooking::getTotalPrice).sum();
    }

    // Phương thức tính tổng số đơn bị hủy
    public long calculateTotalCancelledBookings(LocalDateTime startDate, LocalDateTime endDate) {
        List<TourBooking> cancelledBookings = tourBookingRepository
                .findByBookingDateBetweenAndStatus(startDate, endDate, BookingStatus.CANCELED);
        return cancelledBookings.size();
    }
    // Phương thức tính tổng số đơn hoàn thành
    public long calculateTotalCompletedBookings(LocalDateTime startDate, LocalDateTime endDate) {
        List<TourBooking> completedBookings = tourBookingRepository
                .findByBookingDateBetweenAndStatus(startDate, endDate, BookingStatus.COMPLETED);
        return completedBookings.size();
    }

    // Phương thức tính tổng số đơn xác nhạn
    public long calculateTotalConfirmedBookings(LocalDateTime startDate, LocalDateTime endDate) {
        List<TourBooking> cònirmedBookings = tourBookingRepository
                .findByBookingDateBetweenAndStatus(startDate, endDate, BookingStatus.CONFIRMED);
        return cònirmedBookings.size();
    }

    // Phương thức tính tổng số đơn đã thanh toán
    public long calculateTotalPaidBookings(LocalDateTime startDate, LocalDateTime endDate) {
        List<TourBooking> cònirmedBookings = tourBookingRepository
                .findByBookingDateBetweenAndStatus(startDate, endDate, BookingStatus.PAID);
        return cònirmedBookings.size();
    }

    // Phương thức tính tổng số đơn đã thuc hiện
    public long calculateTotalProgressBookings(LocalDateTime startDate, LocalDateTime endDate) {
        List<TourBooking> cònirmedBookings = tourBookingRepository
                .findByBookingDateBetweenAndStatus(startDate, endDate, BookingStatus.IN_PROGRESS);
        return cònirmedBookings.size();
    }

    public List<TourBookingDTO> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<TourBooking> bookings =  tourBookingRepository.findBookingsByDateRange(startDate, endDate);
//        List<TourBooking> bookings = tourBookingRepository.findAll();

        return bookings.stream().map(booking -> {
            // Lấy thông tin Customer và Tour
            String customerName = booking.getCustomer().getFullName();  // Giả sử Customer có trường 'name'
            String tourName = booking.getTour().getName();  // Giả sử Tour có trường 'name'

            return new TourBookingDTO(
                    booking.getBookingId(),
                    booking.getNumberPeople(),
                    booking.getTotalPrice(),
                    booking.getBookingDate(),
                    booking.getStatus().toString(),
                    booking.getTour().getName(),
                    booking.getTour().getImageURL(),
                    booking.getCustomer().getFullName()
            );
        }).collect(Collectors.toList());
    }

    public List<TopTourBookingDTO> getTopTourBookings(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Lấy top tour theo số lượng đặt trong khoảng thời gian
        List<Object[]> rawResults = tourBookingRepository.findTopToursWithBookingCounts(startDateTime, endDateTime, PageRequest.of(0, 10));

        Map<String, TopTourBookingDTO> resultMap = new HashMap<>();

        // Xử lý kết quả trả về từ query
        for (Object[] row : rawResults) {
            String tourName = (String) row[1];

            // Chuyển đổi bookingDate (java.sql.Date) thành LocalDate
            java.sql.Date sqlDate = (java.sql.Date) row[2]; // Lấy giá trị ngày
            LocalDate bookingDate = sqlDate.toLocalDate(); // Chuyển sang LocalDate

            Long count = (Long) row[3];

            // Lấy tour hiện tại trong kết quả (hoặc tạo mới nếu chưa có)
            TopTourBookingDTO tourData = resultMap.computeIfAbsent(tourName, name -> new TopTourBookingDTO(name, new ArrayList<>()));

            // Thêm dữ liệu vào tour
            tourData.getBookingData().add(new BookingCountByDate(bookingDate, count));
        }

        return new ArrayList<>(resultMap.values());
    }

    //doanh thu tour
    public List<TourRevenueDTO> getTopTourRevenue(LocalDate start, LocalDate end) {
        // Mặc định sử dụng các trạng thái cần tính doanh thu
        List<BookingStatus> statuses = List.of(
                BookingStatus.PAID,
                BookingStatus.COMPLETED,
                BookingStatus.IN_PROGRESS
        );
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        List<TourBooking> bookings = tourBookingRepository.findByDateRangeAndStatus(startDateTime, endDateTime, statuses);
        Map<Tour, List<TourBooking>> bookingsByTour = bookings.stream().collect(Collectors.groupingBy(TourBooking::getTour));

        return bookingsByTour.entrySet().stream().map(entry -> {
            Tour tour = entry.getKey();
            List<TourBooking> tourBookings = entry.getValue();

            long totalBookings = tourBookings.size();
            long totalCancelled = tourBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CANCELED)
                    .count();

//            double totalRevenue = bookings.stream()
//                    .mapToDouble(booking -> {
//                        double paid = booking.pay() != null ? booking.getPayment().getAmountPaid() : 0.0;
//                        double refund = booking.getPayment() != null && booking.getPayment().getRefund() != null
//                                ? booking.getPayment().getRefund().getRefundAmount() : 0.0;
//                        return paid - refund;
//                    })
//                    .sum();
            double totalRevenue = bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.PAID
                            || b.getStatus() == BookingStatus.IN_PROGRESS
                            || b.getStatus() == BookingStatus.COMPLETED)
                    .mapToDouble(TourBooking::getTotalPrice)
                    .sum();

            double avgRevenuePerBooking = totalBookings > 0 ? totalRevenue / totalBookings : 0.0;

            Map<BookingStatus, Long> statusCountMap = tourBookings.stream()
                    .collect(Collectors.groupingBy(TourBooking::getStatus, Collectors.counting()));

            BookingStatus mostCommonStatus = statusCountMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            return new TourRevenueDTO(
                    tour.getTourId(),
                    tour.getName(),
                    totalBookings,
                    totalCancelled,
                    totalRevenue,
                    avgRevenuePerBooking
//                    mostCommonStatus != null ? mostCommonStatus.name() : "UNKNOWN"
            );
        }).collect(Collectors.toList());
    }


}
