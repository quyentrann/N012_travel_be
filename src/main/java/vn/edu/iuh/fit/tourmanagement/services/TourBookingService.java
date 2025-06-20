package vn.edu.iuh.fit.tourmanagement.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.dto.*;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.enums.RefundStatus;
import vn.edu.iuh.fit.tourmanagement.models.*;
import vn.edu.iuh.fit.tourmanagement.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private static final Logger logger = LoggerFactory.getLogger(TourBookingService.class);

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
        User user = (User) authentication.getPrincipal();
        int adults = bookingRequest.getNumberAdults();
        int children = bookingRequest.getNumberChildren();
        int infants = bookingRequest.getNumberInfants();
        if (user.getCustomer() == null) {
            throw new Exception("User is not a customer");
        }

        Customer customer = user.getCustomer();

        if (bookingRequest.getFullName() != null) {
            customer.setFullName(bookingRequest.getFullName());
        }
        if (bookingRequest.getPhoneNumber() != null) {
            customer.setPhoneNumber(bookingRequest.getPhoneNumber());
        }
        customerRepository.save(customer);

        Tour tour = tourRepository.findById(bookingRequest.getTourId())
                .orElseThrow(() -> new Exception("Tour not found"));

        if (tour.getAvailableSlot() < bookingRequest.getNumberPeople()) {
            throw new Exception("Not enough available slots");
        }

        int totalParticipants = adults + children + infants;
        if (totalParticipants != bookingRequest.getNumberPeople()) {
            throw new Exception("Total number of adults, children, and infants does not match numberPeople");
        }

        // Kiểm tra ngày khởi hành hợp lệ
        LocalDate departureDate = bookingRequest.getDepartureDate();
        if (departureDate == null) {
            throw new Exception("Departure date is required");
        }
        boolean validDepartureDate = tour.getTourDetails().stream()
                .anyMatch(detail -> detail.getStartDate().equals(departureDate));
        if (!validDepartureDate) {
            throw new Exception("Invalid departure date for this tour");
        }

        // Tạo booking
        TourBooking booking = TourBooking.builder()
                .customer(customer)
                .tour(tour)
                .numberPeople(bookingRequest.getNumberPeople())
                .totalPrice(bookingRequest.getTotalPrice())
                .bookingDate(LocalDateTime.now())
                .departureDate(departureDate) // Lưu departureDate
                .status(BookingStatus.CONFIRMED)
                .numberAdults(adults)
                .numberChildren(children)
                .numberInfants(infants)
                .build();

        TourBooking savedBooking = tourBookingRepository.save(booking);

        tour.setAvailableSlot(tour.getAvailableSlot() - bookingRequest.getNumberPeople());
        tourRepository.save(tour);

        try {
            String departureLocation = tour.getLocation();
            String departureDateStr = departureDate.toString();

            String paymentDeadline = booking.getBookingDate().plusDays(3).toString();

            mailService.sendBookingConfirmationEmail(
                    user.getEmail(),
                    customer.getFullName(),
                    tour.getName(),
                    departureLocation,
                    departureDateStr,
                    bookingRequest.getNumberPeople(),
                    bookingRequest.getTotalPrice(),
                    paymentDeadline
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email xác nhận: " + e.getMessage());
        }

        return savedBooking;
    }


    //Hủy tour
    @Transactional
    public CancelResponse cancelBooking(Long bookingId, String reason, LocalDateTime cancelDate, boolean isHoliday, Authentication authentication) {
        // Kiểm tra quyền
        User user = (User) authentication.getPrincipal();
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy booking"));
        if (user.getCustomer() == null || !booking.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            throw new IllegalStateException("Bạn không có quyền hủy booking này.");
        }

        // Kiểm tra trạng thái
        if (booking.getStatus() == BookingStatus.CANCELED ||
                booking.getStatus() == BookingStatus.COMPLETED ||
                booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Không thể hủy booking với trạng thái hiện tại: " + booking.getStatus());
        }

        // Kiểm tra cancelDate
        LocalDateTime tourStartDate = booking.getTour().getTourDetails().get(0).getStartDate().atStartOfDay();
        if (cancelDate.isAfter(LocalDateTime.now()) || cancelDate.isAfter(tourStartDate)) {
            throw new IllegalArgumentException("Ngày hủy không hợp lệ. Ngày hủy phải trước thời điểm hiện tại và ngày khởi hành của tour.");
        }

        // Tính phí hủy
        double cancellationFee = 0;
        double refundAmount = 0;
        if (booking.getStatus() == BookingStatus.PAID) {
            cancellationFee = calculateCancellationFee(booking, cancelDate, isHoliday);
            refundAmount = booking.getTotalPrice() - cancellationFee;
        }

        // Lưu lịch sử hủy
        BookingHistory history = BookingHistory.builder()
                .booking(booking)
                .oldStatus(booking.getStatus())
                .newStatus(BookingStatus.CANCELED)
                .changeDate(LocalDateTime.now())
                .cancelDate(cancelDate)
                .reason(reason)
                .cancellationFee(cancellationFee)
                .refundAmount(refundAmount)
                .tour(booking.getTour())
                .isHoliday(isHoliday)
                .build();
        bookingHistoryRepository.save(history);

        // Cập nhật trạng thái booking
        booking.setStatus(BookingStatus.CANCELED);
        tourBookingRepository.save(booking);

        // Cập nhật slot tour
        Tour tour = booking.getTour();
        tour.setAvailableSlot(tour.getAvailableSlot() + booking.getNumberPeople());
        tourRepository.save(tour);

        // Gửi email xác nhận hủy
        try {
            mailService.sendCancellationConfirmationEmail(
                    user.getEmail(),
                    booking.getCustomer().getFullName(),
                    tour.getName(),
                    reason,
                    cancellationFee,
                    refundAmount
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email hủy: " + e.getMessage());
        }

        // Trả về kết quả
        CancelResponse response = new CancelResponse();
        response.setMessage("Booking đã được hủy thành công!");
        response.setCancellationFee(cancellationFee);
        response.setRefundAmount(refundAmount);
        return response;
    }




    // Hàm tính phí hủy
    private double calculateCancellationFee(TourBooking booking, LocalDateTime cancelDate, boolean isHoliday) {
        // Kiểm tra tourDetails
        if (booking.getTour().getTourDetails() == null || booking.getTour().getTourDetails().isEmpty()) {
            throw new IllegalStateException("Tour không có thông tin chi tiết về ngày khởi hành.");
        }

        LocalDateTime tourStartDate = booking.getTour().getTourDetails().get(0).getStartDate().atStartOfDay();
        long daysBeforeTour = java.time.temporal.ChronoUnit.DAYS.between(cancelDate, tourStartDate);

        double cancellationFee = 0;
        if (isHoliday) {
            if (daysBeforeTour >= 30) {
                cancellationFee = 0.2 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 15) {
                cancellationFee = 0.4 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                cancellationFee = 0.6 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 3) {
                cancellationFee = 0.8 * booking.getTotalPrice();
            } else {
                cancellationFee = booking.getTotalPrice();
            }
        } else {
            if (daysBeforeTour >= 14) {
                cancellationFee = 0.1 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                cancellationFee = 0.3 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 4) {
                cancellationFee = 0.5 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 1) {
                cancellationFee = 0.7 * booking.getTotalPrice();
            } else {
                cancellationFee = booking.getTotalPrice();
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
        return bookingHistoryRepository.findByBooking_BookingId(bookingId);
    }


    public ChangeTourResponse calculateChangeFee(Long bookingId, ChangeTourRequest request, User user) {
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy booking"));

        if (user.getCustomer() == null || !booking.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            throw new IllegalStateException("Bạn không có quyền thay đổi booking này.");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PAID) {
            throw new IllegalStateException("Chỉ có thể thay đổi booking ở trạng thái CONFIRMED hoặc PAID.");
        }

        Tour tour = booking.getTour();
        if (!tour.getStatus().toString().equals("ACTIVE")) {
            throw new IllegalArgumentException("Tour không còn hoạt động.");
        }

        // Tính tổng số người
        int numberPeople = request.getNumberAdults() + request.getNumberChildren() + request.getNumberInfants();

        // Kiểm tra số chỗ khả dụng
        if (tour.getAvailableSlot() + booking.getNumberPeople() < numberPeople) {
            throw new IllegalArgumentException("Không đủ chỗ cho số người yêu cầu.");
        }

        // Kiểm tra ngày khởi hành mới
        LocalDate newDepartureDate = request.getDepartureDate();
        boolean validDepartureDate = tour.getTourDetails().stream()
                .anyMatch(detail -> detail.getStartDate().equals(newDepartureDate));
        if (!validDepartureDate) {
            throw new IllegalArgumentException("Ngày khởi hành mới không hợp lệ cho tour này.");
        }

        // Tính phí đổi lịch
        LocalDateTime changeDate = request.getChangeDate() != null ? request.getChangeDate() : LocalDateTime.now();
        LocalDateTime departureDate = booking.getDepartureDate().atStartOfDay();
        long daysBeforeTour = java.time.temporal.ChronoUnit.DAYS.between(changeDate, departureDate);

        // Kiểm tra thời gian đổi
        if (request.isHoliday() && daysBeforeTour < 3) {
            throw new IllegalArgumentException("Không thể thay đổi lịch trước 3 ngày trong dịp lễ.");
        } else if (!request.isHoliday() && daysBeforeTour < 1) {
            throw new IllegalArgumentException("Không thể thay đổi lịch trước 1 ngày.");
        }

        double changeFee = calculateCancellationFee(booking, changeDate, request.isHoliday());

        // Tính giá mới
        double basePrice = tour.getPrice();
        double adultPrice = basePrice;
        double childPrice = basePrice * 0.85;
        double infantPrice = basePrice * 0.30;
        double holidayMultiplier = request.isHoliday() ? 1.2 : 1.0;

        double newTotalPrice = (adultPrice * request.getNumberAdults() +
                childPrice * request.getNumberChildren() +
                infantPrice * request.getNumberInfants()) * holidayMultiplier;

        // Áp dụng giảm giá 10% nếu >= 5 người
        int totalParticipants = request.getNumberAdults() + request.getNumberChildren() + request.getNumberInfants();
        if (totalParticipants >= 5) {
            newTotalPrice *= 0.9;
        }

        // Tính chênh lệch giá
        double priceDifference = newTotalPrice - booking.getTotalPrice();
        double refundAmount = 0;

        if (booking.getStatus() == BookingStatus.PAID) {
            if (priceDifference < 0) {
                refundAmount = Math.abs(priceDifference) - changeFee;
                if (refundAmount < 0) {
                    refundAmount = 0;
                }
            }
        }

        ChangeTourResponse response = new ChangeTourResponse();
        response.setMessage("Thông tin phí thay đổi lịch tour");
        response.setChangeFee(changeFee);
        response.setPriceDifference(priceDifference);
        response.setNewTotalPrice(newTotalPrice + (priceDifference > 0 ? changeFee : 0));
        response.setRefundAmount(refundAmount);
        return response;
    }

    @Transactional
    public TourBooking changeTour(Long bookingId, ChangeTourRequest request, User user) {
        logger.info("Starting changeTour for bookingId: {}, user: {}", bookingId, user.getUsername());

        // Validate user permissions
        TourBooking booking = tourBookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking không tồn tại: " + bookingId));
        if (user.getCustomer() == null || !booking.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            logger.error("User {} does not have permission to change booking {}", user.getUsername(), bookingId);
            throw new IllegalStateException("Bạn không có quyền đổi tour này");
        }

        // Validate booking status
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PAID) {
            logger.warn("Booking {} is not in CONFIRMED or PAID status: {}", bookingId, booking.getStatus());
            throw new IllegalStateException("Chỉ có thể đổi tour ở trạng thái CONFIRMED hoặc PAID");
        }

        Tour tour = booking.getTour();
        int numberPeople = request.getNumberAdults() + request.getNumberChildren() + request.getNumberInfants();
        if (numberPeople <= 0) {
            logger.error("Invalid number of people: {}", numberPeople);
            throw new IllegalArgumentException("Số lượng người phải lớn hơn 0");
        }

        // Validate departure date
        LocalDate newDepartureDate = request.getDepartureDate();
        boolean validDepartureDate = tour.getTourDetails().stream()
                .anyMatch(detail -> detail.getStartDate().equals(newDepartureDate));
        if (!validDepartureDate) {
            logger.error("Invalid departure date {} for tour {}", newDepartureDate, tour.getTourId());
            throw new IllegalArgumentException("Ngày khởi hành mới không hợp lệ cho tour này");
        }

        // Validate available slots
        if (tour.getAvailableSlot() + booking.getNumberPeople() < numberPeople) {
            logger.error("Not enough slots for bookingId: {}, required: {}, available: {}",
                    bookingId, numberPeople, tour.getAvailableSlot() + booking.getNumberPeople());
            throw new IllegalArgumentException("Không đủ chỗ cho số người yêu cầu");
        }

        // Calculate prices
        double adultPrice = tour.getPrice();
        double childPrice = adultPrice * 0.85;
        double infantPrice = adultPrice * 0.30;
        double holidayMultiplier = request.isHoliday() ? 1.2 : 1.0;

        double newTotalPrice = (adultPrice * request.getNumberAdults() +
                childPrice * request.getNumberChildren() +
                infantPrice * request.getNumberInfants()) * holidayMultiplier;

        // Apply 10% discount if >= 5 people
        if (numberPeople >= 5) {
            newTotalPrice *= 0.9;
        }

        double changeFee = calculateChangeFee(booking, request);
        double priceDifference = newTotalPrice - booking.getTotalPrice();
        double refundAmount = priceDifference < 0 ? Math.abs(priceDifference) - changeFee : 0;
        if (refundAmount < 0) {
            refundAmount = 0;
        }

        // Determine additional payment and status
        double additionalPayment;
        BookingStatus newStatus;
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            additionalPayment = newTotalPrice + changeFee;
            newStatus = additionalPayment > 0 ? BookingStatus.PENDING_PAYMENT : BookingStatus.CONFIRMED;
        } else { // PAID
            additionalPayment = priceDifference > 0 ? priceDifference + changeFee : 0;
            newStatus = additionalPayment > 0 ? BookingStatus.PENDING_PAYMENT : BookingStatus.PAID;
        }

        // Update tour slots
        tour.setAvailableSlot(tour.getAvailableSlot() + booking.getNumberPeople() - numberPeople);
        try {
            tourRepository.save(tour);
            logger.info("Updated tour slots for tourId: {}, new available slots: {}", tour.getTourId(), tour.getAvailableSlot());
        } catch (DataAccessException e) {
            logger.error("Failed to update tour slots for tourId: {}, error: {}", tour.getTourId(), e.getMessage());
            throw new RuntimeException("Failed to update tour slots", e);
        }

        // Update booking
        BookingStatus oldStatus = booking.getStatus();
        booking.setDepartureDate(newDepartureDate);
        booking.setNumberPeople(numberPeople);
        booking.setNumberAdults(request.getNumberAdults());
        booking.setNumberChildren(request.getNumberChildren());
        booking.setNumberInfants(request.getNumberInfants());
        booking.setTotalPrice(newTotalPrice);
        booking.setStatus(newStatus);

        // Create BookingHistory
        BookingHistory history = BookingHistory.builder()
                .booking(booking)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changeDate(LocalDateTime.now())
                .reason("Đổi lịch tour sang ngày " + newDepartureDate + " với " + numberPeople + " người")
                .cancellationFee(changeFee)
                .refundAmount(refundAmount)
                .additionalPayment(additionalPayment)
                .refundStatus(refundAmount > 0 ? RefundStatus.PENDING : RefundStatus.NONE)
                .tour(tour)
                .isHoliday(request.isHoliday())
                .cancelDate(null)
                .build();

        // Save history and booking
        try {
            bookingHistoryRepository.save(history);
            tourBookingRepository.save(booking);
            logger.info("Successfully saved BookingHistory and TourBooking: bookingId={}, newStatus={}, additionalPayment={}",
                    bookingId, newStatus, additionalPayment);
        } catch (DataAccessException e) {
            logger.error("Failed to save BookingHistory or TourBooking for bookingId: {}, error: {}", bookingId, e.getMessage());
            throw new RuntimeException("Failed to save booking changes", e);
        }

        // Send email notification
        try {
            mailService.sendChangeTourConfirmationEmail(
                    user.getEmail(),
                    booking.getCustomer().getFullName(),
                    tour.getName(),
                    newDepartureDate.toString(),
                    numberPeople,
                    newTotalPrice,
                    changeFee,
                    additionalPayment,
                    refundAmount
            );
            logger.info("Change tour email sent to: {}", user.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send change tour email to {}, error: {}", user.getEmail(), e.getMessage());
        }

        return booking;
    }

    private double calculateChangeFee(TourBooking booking, ChangeTourRequest request) {
        LocalDateTime changeDate = request.getChangeDate() != null ? request.getChangeDate() : LocalDateTime.now();
        LocalDateTime departureDate = booking.getDepartureDate().atStartOfDay();
        LocalDateTime bookingDate = booking.getBookingDate();
        long daysBeforeTour = java.time.temporal.ChronoUnit.DAYS.between(changeDate, departureDate);
        long hoursSinceBooking = java.time.temporal.ChronoUnit.HOURS.between(bookingDate, changeDate);

        // Miễn phí đổi trong 24 giờ sau khi đặt tour nếu ngày khởi hành còn xa
        if (hoursSinceBooking <= 24 && daysBeforeTour >= 7) {
            return 0.0; // Miễn phí đổi tour
        }

        // Không cho đổi tour nếu < 7 ngày trước khởi hành
        if (daysBeforeTour < 7) {
            throw new IllegalArgumentException("Không thể đổi tour khi còn dưới 7 ngày trước ngày khởi hành.");
        }

        double changeFee = 0;
        if (request.isHoliday()) {
            if (daysBeforeTour >= 30) {
                changeFee = 0.15 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 15) {
                changeFee = 0.2 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                changeFee = 0.3 * booking.getTotalPrice();
            }
        } else {
            if (daysBeforeTour >= 14) {
                changeFee = 0.1 * booking.getTotalPrice();
            } else if (daysBeforeTour >= 7) {
                changeFee = 0.2 * booking.getTotalPrice();
            }
        }
        return changeFee;
    }

    private void validateBookingStatus(BookingStatus status, String fieldName) {
        if (status == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
}


