package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.dto.CancelRequest;
import vn.edu.iuh.fit.tourmanagement.dto.CancelResponse;
import vn.edu.iuh.fit.tourmanagement.dto.TourBookingDetailDTO;
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

    @Autowired
    private MailService mailService; // Thêm MailService vào

    public TourBookingService(TourBookingRepository tourBookingRepository) {
        this.tourBookingRepository = tourBookingRepository;
    }

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
        // Lấy User từ Authentication
        User user = (User) authentication.getPrincipal();

        if (user.getCustomer() == null) {
            throw new Exception("User is not a customer");
        }

        Customer customer = user.getCustomer();

        // Cập nhật thông tin khách hàng nếu có
        if (bookingRequest.getFullName() != null) {
            customer.setFullName(bookingRequest.getFullName());
        }
        if (bookingRequest.getPhoneNumber() != null) {
            customer.setPhoneNumber(bookingRequest.getPhoneNumber());
        }

        customerRepository.save(customer); // Lưu thông tin mới

        // Lấy thông tin tour
        Tour tour = tourRepository.findById(bookingRequest.getTourId())
                .orElseThrow(() -> new Exception("Tour not found"));

        // Kiểm tra số lượng chỗ trống
        if (tour.getAvailableSlot() < bookingRequest.getNumberPeople()) {
            throw new Exception("Not enough available slots");
        }

        // Nếu là ngày lễ, tăng giá
//        double totalPrice = bookingRequest.getTotalPrice();
//        if (bookingRequest.isHoliday()) {
//            double holidayMultiplier = 1.2;  // Giả sử tăng 20% vào giá tour
//            totalPrice = totalPrice * holidayMultiplier;
//        }

        // Tạo booking
        TourBooking booking = TourBooking.builder()
                .customer(customer)
                .tour(tour)
                .numberPeople(bookingRequest.getNumberPeople())
//                .totalPrice(totalPrice)
                .totalPrice(bookingRequest.getTotalPrice())
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        // Lưu booking
        TourBooking savedBooking = tourBookingRepository.save(booking);

        // Cập nhật slot tour
        tour.setAvailableSlot(tour.getAvailableSlot() - bookingRequest.getNumberPeople());
        tourRepository.save(tour);
        // **Gửi email xác nhận đặt tour**
        try {
            String departureLocation = tour.getLocation(); // Nơi khởi hành
            String departureDate = "Ngày khởi hành không xác định";  // Đặt mặc định
            if (tour.getTourDetails() != null && !tour.getTourDetails().isEmpty()) {
                departureDate = tour.getTourDetails().get(0).getStartDate().toString();
            }

            String paymentDeadline = booking.getBookingDate().plusDays(3).toString(); // Giả sử hạn thanh toán là 3 ngày sau

            // Gửi email với thông tin chi tiết bổ sung
            mailService.sendBookingConfirmationEmail(
                    user.getEmail(), // Lấy email từ User
                    customer.getFullName(), // Tên khách hàng
                    tour.getName(), // Tên tour
                    departureLocation, // Nơi khởi hành
                    departureDate, // Ngày khởi hành
                    bookingRequest.getNumberPeople(), // Số người tham gia
                    bookingRequest.getTotalPrice(), // Tổng số tiền
                    paymentDeadline // Hạn thanh toán
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email xác nhận: " + e.getMessage());
        }


        return savedBooking;
    }


    //Hủy tour
    @Transactional
    public CancelResponse cancelBooking(Long bookingId, String reason, LocalDateTime cancelDate, boolean isHoliday) {
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy booking"));

        // Kiểm tra trạng thái của booking, không cho hủy nếu đã hủy, hoàn thành, hoặc đang thực hiện tour
        if (booking.getStatus() == BookingStatus.CANCELED ||
                booking.getStatus() == BookingStatus.COMPLETED ||
                booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Không thể hủy booking với trạng thái hiện tại: " + booking.getStatus());
        }

        // Nếu chưa thanh toán, không hoàn tiền khi hủy
        double cancellationFee = 0;
        double refundAmount = 0;

        if (booking.getStatus() == BookingStatus.PAID) {
            // Nếu đã thanh toán, tính phí hủy và số tiền hoàn lại
            cancellationFee = calculateCancellationFee(booking, cancelDate, isHoliday);
            refundAmount = booking.getTotalPrice() - cancellationFee;
        } else {
            // Nếu chưa thanh toán, không hoàn lại tiền
            refundAmount = 0;
        }

        // Lưu lịch sử hủy
        BookingHistory history = BookingHistory.builder()
                .booking(booking)
                .oldStatus(booking.getStatus())
                .newStatus(BookingStatus.CANCELED)
                .changeDate(LocalDateTime.now())
                .reason(reason)
                .cancellationFee(cancellationFee)
                .refundAmount(refundAmount)  // Lưu số tiền hoàn lại, nếu có
                .tour(booking.getTour())
                .build();
        bookingHistoryRepository.save(history);

        // Cập nhật trạng thái của booking
        booking.setStatus(BookingStatus.CANCELED);
        tourBookingRepository.save(booking);

        // Trả về đối tượng CancelResponse chứa thông tin chi tiết
        CancelResponse response = new CancelResponse();
        response.setMessage("Booking đã được hủy thành công!");
        response.setCancellationFee(cancellationFee);
        response.setRefundAmount(refundAmount);  // Số tiền hoàn lại nếu có

        return response;
    }





    // Hàm tính phí hủy
    private double calculateCancellationFee(TourBooking booking, LocalDateTime cancelDate, boolean isHoliday) {
        // Lấy ngày khởi hành của tour (ví dụ từ tourDetails)
        LocalDateTime tourStartDate = booking.getTour().getTourDetails().get(0).getStartDate().atStartOfDay(); // Giả sử lấy ngày khởi hành của tour đầu tiên và chuyển về LocalDateTime nếu tourDetail dùng LocalDate

        // Tính số ngày trước khi tour bắt đầu
        long daysBeforeTour = java.time.temporal.ChronoUnit.DAYS.between(cancelDate, tourStartDate);

        double cancellationFee = 0;
        if (isHoliday) {
            // Phí hủy cho ngày lễ
            if (daysBeforeTour >= 30) {
                cancellationFee = 0.2 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 15) {
                cancellationFee = 0.4 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                cancellationFee = 0.6 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 3) {
                cancellationFee = 0.8 * booking.getTotalPrice();
            } else {
                cancellationFee = booking.getTotalPrice();  // 100% phí
            }
        } else {
            // Phí hủy cho ngày thường
            if (daysBeforeTour >= 14) {
                cancellationFee = 0.1 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                cancellationFee = 0.3 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 4) {
                cancellationFee = 0.5 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 1) {
                cancellationFee = 0.7 * booking.getTotalPrice();
            } else {
                cancellationFee = booking.getTotalPrice();  // 100% phí
            }
        }

        return cancellationFee;
    }
    public Optional<TourBooking> getBookingById(Long id) {
        return tourBookingRepository.findById(id);
    }

    public TourBooking updateBooking(TourBooking booking) {
        return tourBookingRepository.save(booking);
    }

    public TourBookingDetailDTO getTourBookingDetailById(Long bookingId) {
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        // Tạo TourBookingDetailDTO từ TourBooking
        return new TourBookingDetailDTO(booking);
    }

    public void updatePaymentStatus(Long bookingId, boolean isSuccess) {
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking không tồn tại"));

        if (isSuccess) {
            if (booking.getStatus() != BookingStatus.PAID) {
                booking.setStatus(BookingStatus.PAID);
                tourBookingRepository.save(booking);
            } else {
                throw new IllegalStateException("Booking đã được thanh toán.");
            }
        } else {
            if (booking.getStatus() != BookingStatus.CANCELED) {
                booking.setStatus(BookingStatus.CANCELED);
                tourBookingRepository.save(booking);
            } else {
                throw new IllegalStateException("Booking đã bị hủy.");
            }
        }
    }


    public List<BookingHistory> getBookingHistory(Long bookingId) {
        return bookingHistoryRepository.findByTour_TourId(bookingId);
    }
}
