package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.dto.tour.TourRequest;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;
import vn.edu.iuh.fit.tourmanagement.services.TourCategoryService;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    @Autowired
    private TourService tourService;
    @Autowired
    private TourCategoryService categoryService;

    @Autowired
    private TourRepository tourRepository;

    @GetMapping
    public ResponseEntity<List<Tour>> getAllTours() {
        List<Tour> tours = tourService.getAllTours();
        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tour> getTourById( @PathVariable Long id) {
        Tour tour = tourService.getTourById(id);
        if (tour == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tour, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Tour> createTour(@RequestBody Tour tour) {
        try {
            Tour createdTour = tourService.createTour(tour);
            return new ResponseEntity<>(createdTour, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tour> updateTour(@PathVariable("id") Long id, @RequestBody Tour tour) {
        Tour existingTour = tourService.getTourById(id);
        if (existingTour == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        tour.setTourId(id);
        Tour updatedTour = tourService.updateTour(tour);
        return new ResponseEntity<>(updatedTour, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTour(@PathVariable("id") Long id) {
        boolean isDeleted = tourService.deleteTour(id);
        if (!isDeleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // update thông qua request
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTourRequest(@PathVariable("id") Long id, @RequestBody TourRequest tourRequest) {
        Optional<Tour> optionalTour = tourService.findById(id);

        if (optionalTour.isPresent()) {
            Tour tour = optionalTour.get();
            tour.setName(tourRequest.getName());
            tour.setLocation(tourRequest.getLocation());
            tour.setPrice(tourRequest.getPrice());
            tour.setAvailableSlot(tourRequest.getAvailableSlot());
            tour.setDescription(tourRequest.getDescription());
            // Chuyển chuỗi sang Enum
            try {
                TourStatus status = TourStatus.valueOf(tourRequest.getStatus().toUpperCase());
                tour.setStatus(status);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Trạng thái không hợp lệ!");
            }
            tour.setImageURL(tourRequest.getImageURL());

            // Cập nhật loại tour từ categoryId
            TourCategory category = categoryService.getTourCategoryById(tourRequest.getTourcategoryId());

            if (category == null) {
                throw new RuntimeException("Không tìm thấy loại tour!");
            }
            tour.setTourcategory(category);

            tourService.updateTour(tour);
            return ResponseEntity.ok("Cập nhật tour thành công!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy tour!");
        }
    }

    @PostMapping("/category/{categoryId}")
    public ResponseEntity<Tour> createTour(@PathVariable Long categoryId, @RequestBody Tour tour) {
        TourCategory category = categoryService.getTourCategoryById(categoryId);
        if (category == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        tour.setTourcategory(category);
        Tour createdTour = tourService.createTour(tour);
        return new ResponseEntity<>(createdTour, HttpStatus.CREATED);
    }
}
