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



//    public boolean sendOtpEmail(String toEmail) {
//        try {
//            // Nếu đã có OTP nhưng chưa hết hạn, không gửi lại
//            OtpInfo existingOtp = otpStore.get(toEmail);
//            if (existingOtp != null && existingOtp.getExpiryTime() > System.currentTimeMillis()) {
//                System.out.println("OTP for this email is still valid.");
//                return false;
//            }
//
//            // Xóa OTP cũ nếu có
//            otpStore.remove(toEmail);
//
//            // Tạo OTP mới
//            String otp = generateOtp();
//            long expiryTime = System.currentTimeMillis() + OTP_EXPIRY_TIME;
//
//            // Lưu OTP mới
//            otpStore.put(toEmail, new OtpInfo(otp, expiryTime));
//
//            // Gửi OTP qua email
//            sendOtpWithOtpCode(toEmail, otp);
//            return true;
//        } catch (Exception e) {
//            System.err.println("Error sending OTP: " + e.getMessage());
//            return false;
//        }
//    }

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


    // Sinh OTP ngẫu nhiên
    private String generateOtp() {
        // Tạo số ngẫu nhiên từ 100000 đến 999999 (6 chữ số)
        int otp = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(otp); // Chuyển đổi số thành chuỗi
    }

    // Kiểm tra OTP khi người dùng nhập
    public boolean verifyOtp(String email, String otp) {
        OtpInfo otpInfo = otpStore.get(email);

        System.out.println("User input OTP: " + otp);
        System.out.println("Stored OTP: " + (otpInfo != null ? otpInfo.getOtp() : "NULL"));
        System.out.println("Stored Expiry Time: " + (otpInfo != null ? otpInfo.getExpiryTime() : "NULL"));

        if (otpInfo == null) {
            return false; // Không tìm thấy OTP
        }

        if (!otpInfo.getOtp().equals(otp)) {
            System.out.println("OTP does not match.");
            return false;
        }

        if (otpInfo.getExpiryTime() <= System.currentTimeMillis()) {
            System.out.println("OTP expired.");
            return false;
        }

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
}

