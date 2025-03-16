package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    @Autowired
    private TourService tourService;

    // Lấy tất cả tour
    @GetMapping
    public ResponseEntity<List<Tour>> getAllTours() {
        List<Tour> tours = tourService.getAllTours();
        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }

    // Lấy tour theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Tour> getTourById(@PathVariable("id") Long id) {
        Tour tour = tourService.getTourById(id);
        if (tour == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tour, HttpStatus.OK);
    }

    // Tạo tour mới
    @PostMapping
    public ResponseEntity<Tour> createTour(@RequestBody Tour tour) {
        try {
            Tour createdTour = tourService.createTour(tour);
            return new ResponseEntity<>(createdTour, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Cập nhật tour
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

    // Xóa tour (có thể là soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTour(@PathVariable("id") Long id) {
        boolean isDeleted = tourService.deleteTour(id);
        if (!isDeleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
