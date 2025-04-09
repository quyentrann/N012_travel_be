package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.edu.iuh.fit.tourmanagement.dto.tour.TourRequest;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;
import vn.edu.iuh.fit.tourmanagement.services.TourCategoryService;

import vn.edu.iuh.fit.tourmanagement.dto.*;

import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import java.util.Optional;

import java.util.stream.Collectors;


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
    public ResponseEntity<List<TourDTO>> getAllTours() {
        List<Tour> tours = tourService.getAllTours();
        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<TourDTO> tourDTOs = tours.stream()
                .map(tour -> new TourDTO(
                        tour.getTourId(),
                        tour.getName(),
                        tour.getPrice(),
                        tour.getAvailableSlot(),
                        tour.getLocation(),
                        tour.getDescription(),
                        tour.getHighlights(),  // Thêm highlights
                        tour.getImageURL(),  // Thêm imageURL
                        tour.getExperiences(),  // Thêm experiences
                        tour.getStatus().name(),  // Thêm status
                        new TourCategoryDTO(
                                tour.getTourcategory().getCategoryId(),
                                tour.getTourcategory().getCategoryName(),
                                tour.getTourcategory().getDescription()
                        ),  // Thêm tour category
                        tour.getTourDetails().stream()
                                .map(detail -> new TourDetailDTO(
                                        detail.getDetailId(),
                                        detail.getStartDate(),
                                        detail.getEndDate(),
                                        detail.getIncludedServices(),
                                        detail.getExcludedServices()
                                ))
                                .collect(Collectors.toList()),
                        tour.getTourSchedules().stream()
                                .map(schedule -> new TourScheduleDTO(
                                        schedule.getScheduleId(),
                                        schedule.getDayNumber(),
                                        schedule.getLocation(),
                                        schedule.getStransport(),
                                        schedule.getActivities()
                                ))
                                .collect(Collectors.toList()),
                        tour.getReviews().stream()
                                .map(review -> new ReviewDTO(
                                        review.getReviewId(),
                                        review.getComment(),
                                        review.getRating(),
                                        review.getReviewDate(),
                                        review.getCustomer() != null ? review.getCustomer().getFullName() : null,
                                        review.getCustomer() != null ? review.getCustomer().getAvatarUrl() : null
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return new ResponseEntity<>(tourDTOs, HttpStatus.OK);
    }



    @GetMapping("/{id}")
    public ResponseEntity<TourDTO> getTourById(@PathVariable Long id) {
        Tour tour = tourService.getTourById(id);
        if (tour == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        TourDTO tourDTO = new TourDTO(
                tour.getTourId(),
                tour.getName(),
                tour.getPrice(),
                tour.getAvailableSlot(),
                tour.getLocation(),
                tour.getDescription(),
                tour.getHighlights(),  // Thêm highlights
                tour.getImageURL(),  // Thêm imageURL
                tour.getExperiences(),
                tour.getStatus().name(),  // Thêm status
                new TourCategoryDTO(
                        tour.getTourcategory().getCategoryId(),
                        tour.getTourcategory().getCategoryName(),
                        tour.getTourcategory().getDescription()
                ),
                tour.getTourDetails().stream()
                        .map(detail -> new TourDetailDTO(
                                detail.getDetailId(),
                                detail.getStartDate(),
                                detail.getEndDate(),
                                detail.getIncludedServices(),
                                detail.getExcludedServices()
                        ))
                        .collect(Collectors.toList()),
                tour.getTourSchedules().stream()
                        .map(schedule -> new TourScheduleDTO(
                                schedule.getScheduleId(),
                                schedule.getDayNumber(),
                                schedule.getLocation(),
                                schedule.getStransport(),
                                schedule.getActivities()
                        ))
                        .collect(Collectors.toList()),
                tour.getReviews().stream()
                        .map(review -> new ReviewDTO(
                                review.getReviewId(),
                                review.getComment(),
                                review.getRating(),
                                review.getReviewDate(),
                                review.getCustomer() != null ? review.getCustomer().getFullName() : null,
                                review.getCustomer() != null ? review.getCustomer().getAvatarUrl() : null
                        ))
                        .collect(Collectors.toList())
        );

        return new ResponseEntity<>(tourDTO, HttpStatus.OK);
    }

    @GetMapping("/{tourId}/similar")
    public ResponseEntity<List<TourDTO>> getSimilarTours(@PathVariable Long tourId) {
        // Lấy danh sách tour tương tự từ service
        List<Tour> similarTours = tourService.getSimilarTours(tourId);

        // Kiểm tra xem danh sách có rỗng không
        if (similarTours.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
        }

        // Chuyển đổi từ Tour thành TourDTO
        List<TourDTO> tourDTOs = similarTours.stream()
                .map(tour -> new TourDTO(
                        tour.getTourId(),
                        tour.getName(),
                        tour.getPrice(),
                        tour.getAvailableSlot(),
                        tour.getLocation(),
                        tour.getDescription(),
                        tour.getHighlights(),
                        tour.getImageURL(),
                        tour.getExperiences(),
                        tour.getStatus().name(),
                        new TourCategoryDTO(
                                tour.getTourcategory().getCategoryId(),
                                tour.getTourcategory().getCategoryName(),
                                tour.getTourcategory().getDescription()
                        ),
                        tour.getTourDetails().stream()
                                .map(detail -> new TourDetailDTO(
                                        detail.getDetailId(),
                                        detail.getStartDate(),
                                        detail.getEndDate(),
                                        detail.getIncludedServices(),
                                        detail.getExcludedServices()
                                ))
                                .collect(Collectors.toList()),
                        tour.getTourSchedules().stream()
                                .map(schedule -> new TourScheduleDTO(
                                        schedule.getScheduleId(),
                                        schedule.getDayNumber(),
                                        schedule.getLocation(),
                                        schedule.getStransport(),
                                        schedule.getActivities()
                                ))
                                .collect(Collectors.toList()),
                        tour.getReviews().stream()
                                .map(review -> new ReviewDTO(
                                        review.getReviewId(),
                                        review.getComment(),
                                        review.getRating(),
                                        review.getReviewDate(),
                                        review.getCustomer() != null ? review.getCustomer().getFullName() : null,
                                        review.getCustomer() != null ? review.getCustomer().getAvatarUrl() : null
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(tourDTOs);
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

    @GetMapping("/suggest")
    public ResponseEntity<List<Tour>> suggestTours(
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "popular", required = false) Boolean popular,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "availableSlots", required = false) Integer availableSlots,
            @RequestParam(value = "experienceType", required = false) String experienceType) {

        List<Tour> tours = tourService.getToursWithFilters(price, location, popular, startDate, duration, availableSlots, experienceType);
        return ResponseEntity.ok(tours);
    }

    // Tìm kiếm các tour theo từ khóa trong tên tour hoặc mô tả
    @GetMapping("/search")
    public ResponseEntity<List<Tour>> search(@RequestParam String keyword) {
        List<Tour> tours = tourService.searchTours(keyword);
        return ResponseEntity.ok(tours);
    }

}
