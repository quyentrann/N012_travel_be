package vn.edu.iuh.fit.tourmanagement.services;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Component
public class JWTService {

    private long EXPIRATION_TIME_REFRESH_TOKEN = 864000000;

    private long EXPIRATION_TIME_ACCESS_TOKEN = 86400000;

    private String SECRET_KEY = "843567893696976453275974432697R634976R738467TR678T34865R6834R8763T478378637664538745673865783678548735687R3";

    /**
     * Generate a Secret Key for HMAC
     * @return SecretKey
     */
    public SecretKey generateKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "HmacSHA512");
    }

    /**
     * Generate JWT token for the given UserDetail and claims
     * @param claims
     * @param userDetails
     * @return JWT token
     */
    public String generateToken(Map<String, Object> claims,UserDetails userDetails) {
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_ACCESS_TOKEN))
                .signWith(generateKey())
                .compact();
    }

    /**
     * Generate JWT token for the given UserDetail
     * @param userDetails
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("user", userDetails.toString());
        return generateToken(claims, userDetails);
    }

    /**
     * Generate a refresh token with custom claims
     * @param claims
     * @param userDetails
     * @return refresh token
     */
    public String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_REFRESH_TOKEN))
                .signWith(generateKey())
                .compact();
    }

    /**
     * Generate a refresh token with custom claims
     * @param userDetails
     * @return refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("user", userDetails.toString());
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_REFRESH_TOKEN))
                .signWith(generateKey())
                .compact();
    }

    /**
     * Extract the username from the JWT token
     * @param token
     * @return username
     */
    public String extractUserName(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Get specific claims from the JWT token
     * @param token
     * @param claimsTFunction
     * @param <T>
     * @return specific claim
     */
    private <T> T getClaim(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(Jwts.parser().setSigningKey(generateKey()).build().parseClaimsJws(token).getBody());
    }

    /**
     * Validate if the token is valid for the given user
     * @param token
     * @param userDetails
     * @return boolean indicating if the token is valid
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Check if the token has expired
     * @param token
     * @return boolean indicating if the token is expired
     */
    public boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Validate if the refreshToken is valid for the given user
     * @param refreshToken
     * @param userDetails
     * @return boolean indicating if the token is valid
     */
    public boolean isRefreshTokenValid(String refreshToken, UserDetails userDetails) {
        final String userName = extractUserName(refreshToken);
        return userName.equals(userDetails.getUsername()) && !isRefreshTokenExpired(refreshToken);
    }

    /**
     * Check if the refreshToken has expired
     * @param refreshToken
     * @return boolean indicating if the token is expired
     */
    public boolean isRefreshTokenExpired(String refreshToken) {
        return getClaim(refreshToken, Claims::getExpiration).before(new Date());
    }
}
