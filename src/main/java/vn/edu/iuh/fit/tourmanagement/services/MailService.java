package vn.edu.iuh.fit.tourmanagement.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Lưu OTP và thời gian hết hạn vào bộ nhớ
    private Map<String, OtpInfo> otpStore = new HashMap<>();

    // Thời gian OTP hết hạn (tính bằng giây)
    private static final long OTP_EXPIRY_TIME = TimeUnit.MINUTES.toMillis(5); // 5 phút (đúng định dạng milliseconds)

    public void sendPaymentReminderEmail(String to, String fullName, String tourName, double totalPrice, String deadline) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Nhắc nhở thanh toán tour: " + tourName);
        String htmlContent = "<h3>Xin chào " + fullName + ",</h3>" +
                "<p>Bạn đã đặt tour <strong>" + tourName + "</strong> với tổng giá: <strong>" + totalPrice + " VND</strong>.</p>" +
                "<p>Vui lòng thanh toán trước <strong>" + deadline + "</strong> để xác nhận booking.</p>" +
                "<p>Nếu không thanh toán đúng hạn, booking sẽ tự động bị hủy.</p>" +
                "<p>Trân trọng,<br>Travel TADA</p>";
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public boolean sendOtpEmail(String toEmail) {
        try {
            if (otpStore.containsKey(toEmail)) {
                System.out.println("Old OTP: " + otpStore.get(toEmail).getOtp());
            }

            // Xóa OTP cũ trước khi tạo OTP mới
            otpStore.remove(toEmail);
            System.out.println("Old OTP removed.");

            // Tạo OTP mới
            String otp = generateOtp();
            long expiryTime = System.currentTimeMillis() + OTP_EXPIRY_TIME;

            // Lưu OTP mới
            otpStore.put(toEmail, new OtpInfo(otp, expiryTime));

            System.out.println("New OTP: " + otp); // Log OTP mới

            // Gửi OTP qua email
            sendOtpWithOtpCode(toEmail, otp);
            return true;
        } catch (Exception e) {
            System.err.println("Error sending OTP: " + e.getMessage());
            return false;
        }
    }



    private void sendOtpWithOtpCode(String toEmail, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your OTP Code");
        helper.setText("Your OTP code is: " + otp);
        System.out.println("OTP has been sent to your email.");
        mailSender.send(message);
    }

    public void sendTourCancellationEmail(String toEmail, String customerName, String tourName, boolean isPaid, double refundAmount) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Thông báo hủy tour: " + tourName);

        String emailContent;
        if (isPaid) {
            emailContent = String.format(
                    "Chào %s,\n\n"
                            + "Chúng tôi rất tiếc phải thông báo rằng tour <strong>%s</strong> đã bị hủy do một số lý do không mong muốn.\n\n"
                            + "Vì bạn đã thanh toán cho tour này, chúng tôi cam kết hoàn lại <strong>100%% số tiền</strong> (%,d VNĐ) trong thời gian sớm nhất.\n\n"
                            + "Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.\n\n"
                            + "Chúng tôi rất xin lỗi vì sự bất tiện này và mong được phục vụ bạn trong các tour tiếp theo.\n\n"
                            + "Trân trọng,\n"
                            + "Đội ngũ hỗ trợ Tour Management.",
                    customerName, tourName, (long) refundAmount
            );
        } else {
            emailContent = String.format(
                    "Chào %s,\n\n"
                            + "Chúng tôi rất tiếc phải thông báo rằng tour <strong>%s</strong> đã bị hủy do một số lý do không mong muốn.\n\n"
                            + "Booking của bạn sẽ được hủy tự động. Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.\n\n"
                            + "Chúng tôi rất xin lỗi vì sự bất tiện này và mong được phục vụ bạn trong các tour tiếp theo.\n\n"
                            + "Trân trọng,\n"
                            + "Đội ngũ hỗ trợ Tour Management.",
                    customerName, tourName
            );
        }

        helper.setText(emailContent, true); // Sử dụng HTML để định dạng
        mailSender.send(message);
        System.out.println("Email thông báo hủy tour đã được gửi tới: " + toEmail);
    }

    public void sendCancellationConfirmationEmail(String toEmail, String customerName, String tourName,
                                                  String reason, double cancellationFee, double refundAmount)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Xác nhận hủy tour");

        String emailContent = String.format(
                "Chào %s,\n\n"
                        + "Yêu cầu hủy tour của bạn đã được xử lý. Dưới đây là thông tin chi tiết:\n\n"
                        + "Tên tour: %s\n"
                        + "Lý do hủy: %s\n"
                        + "Phí hủy: %,d VNĐ\n"
                        + "Số tiền hoàn lại: %,d VNĐ\n\n"
                        + "Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.\n\n"
                        + "Trân trọng,\n"
                        + "Đội ngũ hỗ trợ Tour Management.",
                customerName, tourName, reason, (long) cancellationFee, (long) refundAmount
        );

        helper.setText(emailContent);
        mailSender.send(message);
        System.out.println("Email xác nhận hủy tour đã được gửi tới: " + toEmail);
    }

    public void sendChangeTourConfirmationEmail(String toEmail, String customerName, String tourName,
                                                String newDepartureDate, int numberPeople, double newTotalPrice,
                                                double changeFee, double priceDifference, double refundAmount)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Xác nhận thay đổi lịch tour");

        String emailContent = String.format(
                "Chào %s,\n\n"
                        + "Yêu cầu thay đổi lịch tour của bạn đã được xử lý thành công. Dưới đây là thông tin chi tiết:\n\n"
                        + "Tên tour: %s\n"
                        + "Ngày khởi hành mới: %s\n"
                        + "Số người tham gia: %d\n"
                        + "Tổng giá mới: %,d VNĐ\n"
                        + "Phí thay đổi: %,d VNĐ\n"
                        + "%s\n\n"
                        + "Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với chúng tôi.\n\n"
                        + "Trân trọng,\n"
                        + "Đội ngũ hỗ trợ Tour Management.",
                customerName, tourName, newDepartureDate, numberPeople, (long) newTotalPrice, (long) changeFee,
                priceDifference > 0 ? String.format("Số tiền cần thanh toán thêm: %,d VNĐ", (long) priceDifference) :
                        priceDifference < 0 ? String.format("Số tiền hoàn lại: %,d VNĐ", (long) refundAmount) :
                                "Không có chênh lệch giá."
        );

        helper.setText(emailContent);
        mailSender.send(message);
        System.out.println("Email xác nhận thay đổi lịch tour đã được gửi tới: " + toEmail);
    }

    // Sinh OTP ngẫu nhiên
    private String generateOtp() {
        // Tạo số ngẫu nhiên từ 100000 đến 999999 (6 chữ số)
        int otp = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(otp); // Chuyển đổi số thành chuỗi
    }

    // Kiểm tra OTP khi người dùng nhập
    public boolean verifyOtp(String email, String otp) {
        OtpInfo otpInfo = otpStore.get(email);
        System.out.println("Verifying OTP for email: " + email);
        System.out.println("User input OTP: " + otp);
        System.out.println("Stored OTP: " + (otpInfo != null ? otpInfo.getOtp() : "NULL"));
        System.out.println("Stored Expiry Time: " + (otpInfo != null ? otpInfo.getExpiryTime() : "NULL"));
        System.out.println("Current Time: " + System.currentTimeMillis());

        if (otpInfo == null) {
            System.out.println("No OTP found for email: " + email);
            return false;
        }
        if (!otpInfo.getOtp().equals(otp)) {
            System.out.println("OTP does not match.");
            return false;
        }
        if (otpInfo.getExpiryTime() <= System.currentTimeMillis()) {
            System.out.println("OTP expired.");
            return false;
        }
        System.out.println("OTP verified successfully.");
        return true;
    }

    public void removeOtp(String email) {
        otpStore.remove(email);
    }


    // Thông tin OTP và thời gian hết hạn
    private static class OtpInfo {
        private String otp;
        private long expiryTime;

        public OtpInfo(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }

    // Kiểm tra xem OTP có còn hiệu lực không
    public boolean isOtpValid(String email) {
        OtpInfo otpInfo = otpStore.get(email);
        if (otpInfo == null) {
            return false; // Nếu không có OTP thì không hợp lệ
        }

        // Nếu OTP còn hiệu lực, trả về true
        if (otpInfo.getExpiryTime() > System.currentTimeMillis()) {
            return true;
        }

        // Nếu OTP đã hết hạn, xóa OTP cũ và trả về false
        otpStore.remove(email);
        return false;
    }



    public void sendBookingConfirmationEmail(String toEmail, String customerName, String tourName,
                                             String departureLocation, String departureDate, int numberPeople,
                                             double totalPrice, String paymentDeadline) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Xác nhận đặt tour thành công");

        String emailContent = String.format(
                "Chào %s,\n\n"
                        + "Cảm ơn bạn đã đặt tour với chúng tôi! Dưới đây là thông tin chi tiết về chuyến đi của bạn:\n\n"
                        + "Tên tour: %s\n"
                        + "Nơi khởi hành: %s\n"
                        + "Ngày khởi hành: %s\n"
                        + "Số người tham gia: %d\n"
                        + "Số tiền cần thanh toán: %,d VNĐ\n"
                        + "Hạn thanh toán: %s\n\n"
                        + "Trạng thái: CHƯA THANH TOÁN\n"
                        + "Vui lòng hoàn tất thanh toán trước hạn để giữ chỗ của bạn.\n\n"
                        + "Nếu có bất kỳ thắc mắc nào, hãy liên hệ ngay với chúng tôi.\n\n"
                        + "Chúc bạn có một chuyến đi tuyệt vời!\n"
                        + "Trân trọng,\n"
                        + "Đội ngũ hỗ trợ Tour Management.",
                customerName, tourName, departureLocation, departureDate,
                numberPeople, (long) totalPrice, paymentDeadline
        );

        helper.setText(emailContent);
        mailSender.send(message);
        System.out.println("Email xác nhận đặt tour đã được gửi tới: " + toEmail);
    }
}

