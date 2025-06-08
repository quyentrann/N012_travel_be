package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class VnPayService {

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.secretKey}")
    private String secretKey;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    public String createPayment(Long bookingId, Long totalPrice, String ipAddress) {
        // Generate unique vnp_TxnRef
        String vnp_TxnRef = bookingId + "_" + System.currentTimeMillis();
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(totalPrice * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnp_TxnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + bookingId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder queryBuilder = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = URLEncoder.encode(vnpParams.get(fieldName), StandardCharsets.UTF_8);
            queryBuilder.append(fieldName).append('=').append(value).append('&');
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 1);

        String query = queryBuilder.toString();
        String secureHash = hmacSHA512(secretKey, query);
        query += "&vnp_SecureHash=" + secureHash;

        return vnp_PayUrl + "?" + query;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(keySpec);
            byte[] hashBytes = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hashHex = new StringBuilder();
            for (byte b : hashBytes) {
                hashHex.append(String.format("%02x", b));
            }
            return hashHex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }
}