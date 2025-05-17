package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BookingScheduler {

    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private MailService mailService;

    // Chạy mỗi ngày vào 9:00 sáng
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkUnpaidBookings() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Scheduler chạy lúc: " + now + ", múi giờ: " + java.time.ZoneId.systemDefault());
        List<TourBooking> bookings = tourBookingRepository.findByStatus(BookingStatus.CONFIRMED);
        System.out.println("Tìm thấy " + bookings.size() + " booking CONFIRMED");
        for (TourBooking booking : bookings) {
            if (booking.getBookingDate() == null || booking.getDepartureDate() == null) {
                System.err.println("Booking " + booking.getBookingId() + " có booking_date hoặc departure_date null");
                continue;
            }

            long daysToDeparture = ChronoUnit.DAYS.between(LocalDate.now(), booking.getDepartureDate());
            LocalDateTime deadline;
            LocalDateTime reminderTime;
            if (daysToDeparture <= 14) {
                deadline = booking.getBookingDate().plusHours(24);
                reminderTime = booking.getBookingDate().plusHours(12);
            } else if (daysToDeparture <= 30) {
                deadline = booking.getBookingDate().plusDays(3);
                reminderTime = booking.getBookingDate().plusDays(2);
            } else {
                deadline = booking.getDepartureDate().atStartOfDay().minusDays(14);
                reminderTime = deadline.minusDays(2);
            }

            if (now.isAfter(reminderTime) && now.isBefore(deadline) && !booking.isReminderSent()) {
                System.out.println("Chuẩn bị gửi nhắc nhở cho booking " + booking.getBookingId() + ", reminderTime: " + reminderTime + ", deadline: " + deadline);
                sendReminderEmail(booking, deadline);
                booking.setReminderSent(true);
                try {
                    tourBookingRepository.save(booking);
                    System.out.println("Đã lưu reminder_sent = true cho booking: " + booking.getBookingId());
                } catch (Exception e) {
                    System.err.println("Lỗi lưu reminder_sent cho booking " + booking.getBookingId() + ": " + e.getMessage());
                }
            }

            if (now.isAfter(deadline)) {
                System.out.println("Hủy booking " + booking.getBookingId() + ", deadline: " + deadline + ", now: " + now);
                cancelUnpaidBooking(booking);
            }
        }
    }

    private void sendReminderEmail(TourBooking booking, LocalDateTime deadline) {
        try {
            String email = booking.getCustomer().getUser().getEmail();
            if (email == null || email.isEmpty()) {
                System.err.println("Không có email cho booking: " + booking.getBookingId());
                return;
            }
            System.out.println("Bắt đầu gửi email tới: " + email);
            mailService.sendPaymentReminderEmail(
                    email,
                    booking.getCustomer().getFullName(),
                    booking.getTour().getName(),
                    booking.getTotalPrice(),
                    deadline.toString()
            );
            System.out.println("Gửi email thành công cho booking: " + booking.getBookingId());
        } catch (Exception e) {
            System.err.println("Lỗi gửi email nhắc nhở cho booking " + booking.getBookingId() + ": " + e.getMessage());
        }
    }

    private void cancelUnpaidBooking(TourBooking booking) {
        try {
            booking.setStatus(BookingStatus.CANCELED);
            tourBookingRepository.save(booking);
            Tour tour = booking.getTour();
            tour.setAvailableSlot(tour.getAvailableSlot() + booking.getNumberPeople());
            tourRepository.save(tour);
            String email = booking.getCustomer().getUser().getEmail();
            if (email != null && !email.isEmpty()) {
                mailService.sendCancellationConfirmationEmail(
                        email,
                        booking.getCustomer().getFullName(),
                        booking.getTour().getName(),
                        "Hủy tự động do không thanh toán đúng hạn",
                        0,
                        0
                );
                System.out.println("Gửi email hủy thành công cho booking: " + booking.getBookingId());
            }
        } catch (Exception e) {
            System.err.println("Lỗi hủy booking " + booking.getBookingId() + ": " + e.getMessage());
        }
    }
}