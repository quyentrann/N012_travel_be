package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;
import vn.edu.iuh.fit.tourmanagement.services.HybridRecommendationService;
import vn.edu.iuh.fit.tourmanagement.services.SearchHistoryService;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final Logger logger = Logger.getLogger(RecommendationController.class.getName());

    private final HybridRecommendationService recommendationService;
    private final UserRepository userRepository;
    private final SearchHistoryService searchHistoryService;

    public RecommendationController(
            HybridRecommendationService recommendationService,
            UserRepository userRepository,
            SearchHistoryService searchHistoryService) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getRecommendationsForCurrentUser(Authentication authentication) {
        logger.info("Received request for recommendations/me, authentication: " + (authentication != null ? authentication.getName() : "null"));
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warning("Unauthorized access to recommendations, authentication: " + (authentication != null ? authentication.getDetails() : "null"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();
        logger.info("Processing recommendations for email: " + email);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            logger.warning("User not found for email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            List<Tour> recommendedTours = recommendationService.getRecommendations(user);
            logger.info("Returning " + recommendedTours.size() + " recommendations for user ID: " + user.getId());
            return ResponseEntity.ok(recommendedTours);
        } catch (Exception e) {
            logger.severe("Error getting recommendations for user ID: " + user.getId() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting recommendations: " + e.getMessage());
        }
    }

    @PostMapping("/click/{tourId}")
    public ResponseEntity<?> trackTourClick(@PathVariable Long tourId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warning("Unauthorized access to trackTourClick");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            logger.warning("User not found: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            searchHistoryService.updateTourClick(user, tourId);
            recommendationService.clearCache(user);
            logger.info("Tracked tour click for user ID: " + user.getId() + ", tourId: " + tourId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("Error tracking tour click: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error tracking tour click: " + e.getMessage());
        }
    }
}