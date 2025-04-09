package vn.edu.iuh.fit.tourmanagement.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VnPayUtil {
    public static String createSecureHash(String secretKey, String data) throws NoSuchAlgorithmException {
        String input = data + "&vnp_HashSecret=" + secretKey;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}