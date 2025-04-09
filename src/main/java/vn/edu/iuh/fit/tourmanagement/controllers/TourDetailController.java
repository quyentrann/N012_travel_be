package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;
import vn.edu.iuh.fit.tourmanagement.services.TourDetailService;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tour-details")
public class TourDetailController {
    @Autowired
    private TourDetailService tourDetailService;
    @Autowired
    private TourService tourService;

    @GetMapping
    public ResponseEntity<List<TourDetail>> getAllTourDetail() {
        List<TourDetail> tourDetails = tourDetailService.getAllTourDetail();
        if (tourDetails.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tourDetails);
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<?> getTourDetailByTourId(@PathVariable Long tourId) {
        List<TourDetail> tourDetail = tourDetailService.getTourDetailByTourId(tourId);
        if (tourDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tourDetail);
    }

    @PostMapping("/tour/{tourId}")
    public ResponseEntity<?> createTourDetail(@PathVariable Long tourId, @RequestBody Map<String, Object> payload) {
        Tour tour = tourService.getTourById(tourId);
        if (tour == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tour không tồn tại!");

        // Kiểm tra chi tiết tour có chưa
        TourDetail detail = new TourDetail();
        detail.setTour(tour);
        // Cập nhật từ payload
        detail.setIncludedServices((String) payload.get("includedServices"));
        detail.setExcludedServices((String) payload.get("excludedServices"));
        detail.setStartDate(LocalDate.parse((String) payload.get("startDate")));
        detail.setEndDate(LocalDate.parse((String) payload.get("endDate")));

        TourDetail savedDetail = tourDetailService.createTourDetail(detail);
        return ResponseEntity.ok(savedDetail);
    }

    @PatchMapping("/tour/{tourId}")
    public ResponseEntity<String> partialUpdateTourDetailByTourId(@PathVariable Long tourId, @RequestBody Map<String, Object> updates) {
        tourDetailService.partialUpdateTourDetailByTourId(tourId, updates);
        return ResponseEntity.ok().build();
    }
}
