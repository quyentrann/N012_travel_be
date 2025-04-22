package vn.edu.iuh.fit.tourmanagement.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.TourFavouriteRequestDTO;
import vn.edu.iuh.fit.tourmanagement.exceptions.*;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.services.TourFavouriteService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tour-favourites")
public class TourFavouriteController {

    private static final Logger logger = LoggerFactory.getLogger(TourFavouriteController.class);
    private final TourFavouriteService tourFavouriteService;

    public TourFavouriteController(TourFavouriteService tourFavouriteService) {
        this.tourFavouriteService = tourFavouriteService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> addTourFavourite(
            @RequestBody TourFavouriteRequestDTO requestDTO,
            Authentication authentication
    ) {
        logger.info("POST /api/tour-favourites, tourId: {}", requestDTO.getTourId());
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            logger.warn("Forbidden: no customer info for user {}", user.getEmail());
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Không có thông tin khách hàng"));
        }
        // Kiểm tra thủ công tourId
        if (requestDTO.getTourId() == null) {
            logger.error("Bad request: tourId is null for user {}", user.getEmail());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Thiếu tourId"));
        }
        requestDTO.setCustomerId(user.getCustomer().getCustomerId());
        try {
            String message = tourFavouriteService.addTourFavourite(requestDTO);
            logger.info("Successfully added tour {} to favorites for customer {} (email: {})",
                    requestDTO.getTourId(), user.getCustomer().getCustomerId(), user.getEmail());
            return ResponseEntity.ok(Collections.singletonMap("message", message));
        } catch (CustomerNotFoundException | TourNotFoundException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (TourAlreadyFavoritedException e) {
            logger.warn("Conflict: {}", e.getMessage());
            return ResponseEntity.status(409).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Server error for user {}: {}", user.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getFavouritesByCustomer(Authentication authentication) {
        logger.info("GET /api/tour-favourites");
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            logger.warn("Forbidden: no customer info for user {}", user.getEmail());
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Không có thông tin khách hàng"));
        }
        try {
            List<Tour> tours = tourFavouriteService.getFavouritesByCustomer(user.getCustomer().getCustomerId());
            logger.info("Retrieved {} favorite tours for customer {} (email: {})",
                    tours.size(), user.getCustomer().getCustomerId(), user.getEmail());
            return ResponseEntity.ok(tours);
        } catch (CustomerNotFoundException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Server error for user {}: {}", user.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> removeTourFavourite(
            @RequestBody TourFavouriteRequestDTO requestDTO,
            Authentication authentication
    ) {
        logger.info("DELETE /api/tour-favourites, tourId: {}", requestDTO.getTourId());
        User user = (User) authentication.getPrincipal();
        if (user.getCustomer() == null) {
            logger.warn("Forbidden: no customer info for user {}", user.getEmail());
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Không có thông tin khách hàng"));
        }
        // Kiểm tra thủ công tourId
        if (requestDTO.getTourId() == null) {
            logger.error("Bad request: tourId is null for user {}", user.getEmail());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Thiếu tourId"));
        }
        requestDTO.setCustomerId(user.getCustomer().getCustomerId());
        try {
            String message = tourFavouriteService.removeTourFavourite(requestDTO);
            logger.info("Successfully removed tour {} from favorites for customer {} (email: {})",
                    requestDTO.getTourId(), user.getCustomer().getCustomerId(), user.getEmail());
            return ResponseEntity.ok(Collections.singletonMap("message", message));
        } catch (CustomerNotFoundException | TourNotFoundException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (TourNotFavoritedException e) {
            logger.warn("Conflict: {}", e.getMessage());
            return ResponseEntity.status(409).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Server error for user {}: {}", user.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}