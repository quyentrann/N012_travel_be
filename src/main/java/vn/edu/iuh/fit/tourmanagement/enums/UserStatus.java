package vn.edu.iuh.fit.tourmanagement.enums;

public enum UserStatus {
    PENDING,  // Chờ xác thực OTP
    ACTIVE,   // Đã xác thực và đang hoạt động
    DISABLED, // Đã bị vô hiệu hóa
    BLOCKED   // Bị khóa (ví dụ do vi phạm)
}
