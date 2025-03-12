package vn.edu.iuh.fit.tourmanagement.enums;

public enum BookingStatus {
    CONFIRMED,// đã xác nhn đặt tour và chờ thanh toán
    CANCELED,// đã hủy đặt tour
    COMPLETED,// đã hoàn thành tour
    PAID,// đã thanh toán nhưng chưa hoàn thành tour
    IN_GROGRESS// người dùng đang thực hiện tour
}
