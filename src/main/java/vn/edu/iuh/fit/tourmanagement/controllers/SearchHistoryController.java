package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.RecommendedTourDTO;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.UserRepository;
import vn.edu.iuh.fit.tourmanagement.services.HybridRecommendationService;
import vn.edu.iuh.fit.tourmanagement.services.SearchHistoryService;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

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
    public ResponseEntity<?> searchTours(@RequestParam("query") String query,
                                         @RequestParam(defaultValue = "true") boolean random,
                                         @RequestParam(defaultValue = "false") boolean useTFIDF,
                                         Authentication authentication) {
        try {
            List<Tour> searchResults = useTFIDF ? tourService.searchToursWithTFIDF(query, random)
                    : tourService.searchTours(query, random);
            logger.info("Search query: " + query + ", found " + searchResults.size() + " tours (TF-IDF: " + useTFIDF + ", Random: " + random + ")");

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
            String errorEmail = authentication != null && authentication.isAuthenticated() ? authentication.getName() : "unknown";
            logger.severe("Error processing search for user: " + errorEmail + ", query: " + query + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing search");
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveSearchHistory(@RequestParam("query") String query,
                                               @RequestParam(defaultValue = "true") boolean random,
                                               @RequestParam(defaultValue = "false") boolean useTFIDF,
                                               Authentication authentication) {
        String email = "unknown"; // Giá trị mặc định cho email
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                email = authentication.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    List<Tour> searchResults = useTFIDF ? tourService.searchToursWithTFIDF(query, random)
                            : tourService.searchTours(query, random);
                    searchHistoryService.saveSearch(user, query, searchResults);
                    logger.info("Saved search history for user: " + email + ", query: " + query);
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
            logger.severe("Error saving search history for user: " + email + ", query: " + query + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving search history: " + e.getMessage());
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
            List<RecommendedTourDTO> recommendedTours = searchHistoryService.getRecommendedToursFromHistory(user);
            logger.info("Returning " + recommendedTours.size() + " recommended tours for user ID: " + user.getId());
            return ResponseEntity.ok(recommendedTours);
        } catch (Exception e) {
            logger.severe("Error retrieving recommended tours for user: " + email + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving recommended tours");
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
            logger.severe("Error tracking tour click for user: " + email + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error tracking tour click: " + e.getMessage());
        }
    }
}