package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.TourBookingDTO;
import vn.edu.iuh.fit.tourmanagement.dto.report.*;
import vn.edu.iuh.fit.tourmanagement.dto.tourbooking.TopTourBookingDTO;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.services.ReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalBookings(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate.trim() + " 00:00:00", formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate.trim() + " 23:59:59", formatter);

        long totalBookings = reportService.getTotalBookings(startDateTime, endDateTime);
        return ResponseEntity.ok(totalBookings);
    }
    @GetMapping("/total-revenue")
    public ResponseEntity<Double> getTotalRevenue(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // Chuyển đổi chuỗi ngày thành LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate.trim() + " 00:00:00", formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate.trim() + " 23:59:59", formatter);

        double totalRevenue = reportService.calculateTotalRevenue(startDateTime, endDateTime);
        return ResponseEntity.ok(totalRevenue);
    }

    // API lấy tổng số đơn bị hủy
    @GetMapping("/total-cancelled-bookings")
    public ResponseEntity<Long> getTotalCancelledBookings(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // Chuyển đổi chuỗi ngày thành LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate.trim() + " 00:00:00", formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate.trim() + " 23:59:59", formatter);

        long totalCancelledBookings = reportService.calculateTotalCancelledBookings(startDateTime, endDateTime);
        return ResponseEntity.ok(totalCancelledBookings);
    }

    // API lấy tổng số đơn hoàn thành
    @GetMapping("/total-completed-bookings")
    public ResponseEntity<Long> getTotalCompletedBookings(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // Chuyển đổi chuỗi ngày thành LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate.trim() + " 00:00:00", formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate.trim() + " 23:59:59", formatter);

        long totalCancelledBookings = reportService.calculateTotalCompletedBookings(startDateTime, endDateTime);
        return ResponseEntity.ok(totalCancelledBookings);
    }
    // API lấy tổng số đơn xác nhận
    @GetMapping("/total-confirmed-bookings")
    public ResponseEntity<Long> getTotalConfirmedBookings(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // Chuyển đổi chuỗi ngày thành LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate.trim() + " 00:00:00", formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate.trim() + " 23:59:59", formatter);

        long totalCancelledBookings = reportService.calculateTotalConfirmedBookings(startDateTime, endDateTime);
        return ResponseEntity.ok(totalCancelledBookings);
    }

    // API lấy tổng số đơn xác nhận
    @GetMapping("/total-paid-bookings")
    public ResponseEntity<Long> getTotalPaidBookings(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // Chuyển đổi chuỗi ngày thành LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate.trim() + " 00:00:00", formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate.trim() + " 23:59:59", formatter);

        long totalCancelledBookings = reportService.calculateTotalPaidBookings(startDateTime, endDateTime);
        return ResponseEntity.ok(totalCancelledBookings);
    }
    // API lấy tổng số đơn đang thực hiện
    @GetMapping("/total-progress-bookings")
    public ResponseEntity<Long> getTotalProgressBookings(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // Chuyển đổi chuỗi ngày thành LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(startDate.trim() + " 00:00:00", formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate.trim() + " 23:59:59", formatter);

        long totalCancelledBookings = reportService.calculateTotalProgressBookings(startDateTime, endDateTime);
        return ResponseEntity.ok(totalCancelledBookings);
    }


    @GetMapping
    public List<TourBookingDTOReport> getBookingsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate.trim() + " 00:00:00", formatter);
        LocalDateTime end = LocalDateTime.parse(endDate.trim() + " 23:59:59", formatter);

        return reportService.getBookingsByDateRange(start, end);
    }

    @GetMapping("/top-tour-bookings")
    public ResponseEntity<List<TopTourBookingDTO>> getTopTourBookingsByPeople(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<TopTourBookingDTO> result = reportService.getTopTourBookings(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-tours-revenue")
    public ResponseEntity<List<TourRevenueDTO>> getTopTourRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//            ,@RequestParam(required = false) BookingStatus status
    ) {
        List<TourRevenueDTO> result = reportService.getTopTourRevenue(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tour-revenue")
    public ResponseEntity<?> getTourRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
       List<RevenueTourReport> result = reportService.getRevenueTourReport(startDate, endDate);
        return ResponseEntity.ok(result);
    }

}
