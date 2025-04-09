package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.services.HybridRecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    @Autowired
    private HybridRecommendationService recommendationService;
    @GetMapping("/{userId}")
    public ResponseEntity<List<Tour>> getRecommendations(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        List<Tour> recommendations = recommendationService.getRecommendations(user);
        return ResponseEntity.ok(recommendations);
    }
}
