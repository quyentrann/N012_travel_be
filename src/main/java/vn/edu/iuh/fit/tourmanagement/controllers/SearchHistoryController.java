package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.models.SearchHistory;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;
import vn.edu.iuh.fit.tourmanagement.services.HybridRecommendationService;
import vn.edu.iuh.fit.tourmanagement.services.SearchHistoryService;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/search-history")
public class SearchHistoryController {

    private static final Logger logger = Logger.getLogger(SearchHistoryController.class.getName());

    private final SearchHistoryService searchHistoryService;
    private final UserRepository userRepository;
    private final TourService tourService;
    private final HybridRecommendationService recommendationService;

    public SearchHistoryController(
            SearchHistoryService searchHistoryService,
            UserRepository userRepository,
            TourService tourService,
            HybridRecommendationService recommendationService) {
        this.searchHistoryService = searchHistoryService;
        this.userRepository = userRepository;
        this.tourService = tourService;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchTours(@RequestParam("query") String query, Authentication authentication) {
        try {
            List<Tour> searchResults = tourService.searchTours(query);
            logger.info("Search query: " + query + ", found " + searchResults.size() + " tours");

            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    searchHistoryService.saveSearch(user, query, searchResults);
                    logger.info("Saved search history for user: " + email);
                } else {
                    logger.warning("User not found for email: " + email);
                }
            } else {
                logger.info("No authentication provided, skipping search history save");
            }

            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            logger.severe("Error processing search: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing search");
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveSearchHistory(@RequestParam("query") String query, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    List<Tour> searchResults = tourService.searchTours(query);
                    if (searchResults.isEmpty()) {
                        logger.info("No valid search results for query: " + query + ", skipping save search history");
                        return ResponseEntity.ok("No tours found, search history not saved");
                    }
                    searchHistoryService.saveSearch(user, query, searchResults);
                    logger.info("Saved search history for user: " + email);
                    return ResponseEntity.ok("Search history saved");
                } else {
                    logger.warning("User not found for email: " + email);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                }
            } else {
                logger.info("No authentication provided, skipping search history save");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (Exception e) {
            logger.severe("Error saving search history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving search history");
        }
    }

    @GetMapping("/my-history")
    public ResponseEntity<?> getMySearchHistory(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warning("Unauthorized access to my-history");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            logger.warning("User not found: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            List<SearchHistory> history = searchHistoryService.getUserSearchHistory(user);
            logger.info("Returning search history for user ID: " + user.getId() + ", count: " + history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.severe("Error retrieving search history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving search history");
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