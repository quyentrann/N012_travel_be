package vn.edu.iuh.fit.tourmanagement.enums;

import vn.edu.iuh.fit.tourmanagement.constants.BookingStatusConstants;

public enum BookingStatus {
    CONFIRMED(BookingStatusConstants.CONFIRMED, "Đã xác nhận đặt tour và chờ thanh toán"),
    CANCELED(BookingStatusConstants.CANCELED, "Đã hủy đặt tour"),
    PAID(BookingStatusConstants.PAID, "Đã thanh toán nhưng chưa hoàn thành tour"),
    COMPLETED(BookingStatusConstants.COMPLETED, "Đã hoàn thành tour"),
    IN_PROGRESS(BookingStatusConstants.IN_PROGRESS, "Người dùng đang thực hiện tour"),
    PENDING_PAYMENT(BookingStatusConstants.PENDING_PAYMENT, "Chờ thanh toán thêm do thay đổi lịch");

    private final String value;
    private final String description;

    BookingStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}