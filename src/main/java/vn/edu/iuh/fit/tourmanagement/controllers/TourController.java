package vn.edu.iuh.fit.tourmanagement.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.tourmanagement.dto.tour.TourRequest;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourCategory;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;
import vn.edu.iuh.fit.tourmanagement.services.TourCategoryService;

import vn.edu.iuh.fit.tourmanagement.dto.*;

import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/tours")
public class TourController {

    @Autowired
    private TourService tourService;
    @Autowired
    private TourCategoryService categoryService;
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private TourRepository tourRepository;


    @GetMapping
    public ResponseEntity<List<TourDTO>> getAllTours() {
        List<Tour> tours = tourService.getAllTours();
        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<TourDTO> tourDTOs = tours.stream()
                .map(tour -> {
                    // Fetch bookings
                    Hibernate.initialize(tour.getBookings());
                    return new TourDTO(
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
                                            schedule.getActivities(),
                                            schedule.getMeal(),
                                            schedule.getArrivalTime(),
                                            schedule.getDepartureTime()
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
                                    .collect(Collectors.toList()),
                            tour.getBookings().stream()
                                    .map(booking -> new BookingDTO(
                                            booking.getBookingId(),
                                            booking.getNumberPeople(),
                                            booking.getTotalPrice(),
                                            booking.getBookingDate(),
                                            booking.getStatus().name()
                                    ))
                                    .collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(tourDTOs, HttpStatus.OK);
    }



    @GetMapping("/{id}")
    public ResponseEntity<TourDTO> getTourById(@PathVariable Long id) {
        Tour tour = tourService.getTourById(id);
        if (tour == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Fetch bookings
        Hibernate.initialize(tour.getBookings());

        TourDTO tourDTO = new TourDTO(
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
                                schedule.getActivities(),
                                schedule.getMeal(),
                                schedule.getArrivalTime(),
                                schedule.getDepartureTime()
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
                        .collect(Collectors.toList()),
                tour.getBookings().stream()
                        .map(booking -> new BookingDTO(
                                booking.getBookingId(),
                                booking.getNumberPeople(),
                                booking.getTotalPrice(),
                                booking.getBookingDate(),
                                booking.getStatus().name()
                        ))
                        .collect(Collectors.toList())
        );

        return new ResponseEntity<>(tourDTO, HttpStatus.OK);
    }

    @GetMapping("/{tourId}/similar")
    public ResponseEntity<List<TourDTO>> getSimilarTours(@PathVariable Long tourId) {
        List<Tour> similarTours = tourService.getSimilarTours(tourId);
        if (similarTours.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
        }

        List<TourDTO> tourDTOs = similarTours.stream()
                .map(tour -> {
                    // Fetch bookings
                    Hibernate.initialize(tour.getBookings());
                    return new TourDTO(
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
                                            schedule.getActivities(),
                                            schedule.getMeal(),
                                            schedule.getArrivalTime(),
                                            schedule.getDepartureTime()
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
                                    .collect(Collectors.toList()),
                            tour.getBookings().stream()
                                    .map(booking -> new BookingDTO(
                                            booking.getBookingId(),
                                            booking.getNumberPeople(),
                                            booking.getTotalPrice(),
                                            booking.getBookingDate(),
                                            booking.getStatus().name()
                                    ))
                                    .collect(Collectors.toList())
                    );
                })
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

    @PostMapping("/category")
    public ResponseEntity<Tour> createTour( @RequestBody TourRequest tour) {
//        TourCategory category = categoryService.getTourCategoryById(categoryId);
//        if (category == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        tour.setTourcategory(category);
        Tour createdTour = tourService.addTour(tour);
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
//    @GetMapping("/search")
//    public ResponseEntity<List<Tour>> search(@RequestParam String keyword) {
//        List<Tour> tours = tourService.searchTours(keyword);
//        return ResponseEntity.ok(tours);
//    }

    // API lấy danh sách tour theo loại
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TourDTO>> getToursByCategory(@PathVariable Long categoryId) {
        List<Tour> tours = tourService.getToursByCategory(categoryId);
        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<TourDTO> tourDTOs = tours.stream()
                .map(tour -> {
                    // Fetch bookings
                    Hibernate.initialize(tour.getBookings());
                    return new TourDTO(
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
                                            schedule.getActivities(),
                                            schedule.getMeal(),
                                            schedule.getArrivalTime(),
                                            schedule.getDepartureTime()
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
                                    .collect(Collectors.toList()),
                            tour.getBookings().stream()
                                    .map(booking -> new BookingDTO(
                                            booking.getBookingId(),
                                            booking.getNumberPeople(),
                                            booking.getTotalPrice(),
                                            booking.getBookingDate(),
                                            booking.getStatus().name()
                                    ))
                                    .collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(tourDTOs, HttpStatus.OK);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<TourDTO>> getToursByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice) {
        List<Tour> tours = tourService.getToursByPriceRange(minPrice, maxPrice);
        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<TourDTO> tourDTOs = tours.stream()
                .map(tour -> {
                    // Fetch bookings
                    Hibernate.initialize(tour.getBookings());
                    return new TourDTO(
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
                                            schedule.getActivities(),
                                            schedule.getMeal(),
                                            schedule.getArrivalTime(),
                                            schedule.getDepartureTime()
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
                                    .collect(Collectors.toList()),
                            tour.getBookings().stream()
                                    .map(booking -> new BookingDTO(
                                            booking.getBookingId(),
                                            booking.getNumberPeople(),
                                            booking.getTotalPrice(),
                                            booking.getBookingDate(),
                                            booking.getStatus().name()
                                    ))
                                    .collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(tourDTOs, HttpStatus.OK);
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<List<Tour>> getToursByDateRange(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Kiểm tra nếu không có startDate hoặc endDate thì có thể gọi phương thức khác trong service để lấy tất cả các tour
        List<Tour> tours = tourService.getToursByDateRange(startDate, endDate);

        // Nếu không có tour nào, trả về mã lỗi 204 (No Content)
        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(tours);
    }


    @GetMapping("/filter")
    public ResponseEntity<List<TourDTO>> filterToursByTime(
            @RequestParam(value = "time", required = false) String time) {

        LocalDate startDate = null;
        LocalDate endDate = null;

        switch (time.toLowerCase()) {
            case "today":
                startDate = LocalDate.now();
                endDate = startDate;
                break;
            case "this-week":
                startDate = LocalDate.now().with(DayOfWeek.MONDAY);  // Thứ Hai đầu tuần
                endDate = LocalDate.now().with(DayOfWeek.SUNDAY);   // Chủ Nhật cuối tuần
                break;
            case "this-month":
                startDate = LocalDate.now().withDayOfMonth(1);  // Ngày đầu tháng
                endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());  // Ngày cuối tháng
                break;
            case "next-month":
                startDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);  // Ngày đầu tháng sau
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth()); // Ngày cuối tháng sau
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);  // Nếu tham số không hợp lệ
        }

        // Lọc các tour theo ngày đã tính toán
        List<Tour> tours = tourService.filterToursByTime(startDate, endDate);

        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<TourDTO> filteredTourDTOs = tours.stream()
                .map(tour -> {
                    Hibernate.initialize(tour.getBookings());
                    return new TourDTO(
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
                                            schedule.getActivities(),
                                            schedule.getMeal(),
                                            schedule.getArrivalTime(),
                                            schedule.getDepartureTime()
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
                                    .collect(Collectors.toList()),
                            tour.getBookings().stream()
                                    .map(booking -> new BookingDTO(
                                            booking.getBookingId(),
                                            booking.getNumberPeople(),
                                            booking.getTotalPrice(),
                                            booking.getBookingDate(),
                                            booking.getStatus().name()
                                    ))
                                    .collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(filteredTourDTOs, HttpStatus.OK);
    }

    private TourDTO mapTourToDTO(Tour tour) {
        return new TourDTO(
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
                                schedule.getActivities(),
                                schedule.getMeal(),
                                schedule.getArrivalTime(),
                                schedule.getDepartureTime()
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
                        .collect(Collectors.toList()),
                tour.getBookings().stream()
                        .map(booking -> new BookingDTO(
                                booking.getBookingId(),
                                booking.getNumberPeople(),
                                booking.getTotalPrice(),
                                booking.getBookingDate(),
                                booking.getStatus().name()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/filter-by-date")
    public ResponseEntity<List<TourDTO>> filterToursByDate(
            @RequestParam(value = "date", required = true) String date) {

        LocalDate filterDate = LocalDate.parse(date);  // Parse ngày từ tham số vào

        // Lọc các tour có ngày bắt đầu hoặc ngày kết thúc trùng với filterDate
        List<Tour> tours = tourService.filterToursByDate(filterDate);

        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<TourDTO> filteredTourDTOs = tours.stream()
                .map(tour -> mapTourToDTO(tour))
                .collect(Collectors.toList());

        return new ResponseEntity<>(filteredTourDTOs, HttpStatus.OK);
    }


    @GetMapping("/filter-by-month")
    public ResponseEntity<List<TourDTO>> filterToursByMonth(
            @RequestParam(value = "month", required = true) int month,
            @RequestParam(value = "year", required = true) int year) {

        LocalDate startOfMonth = LocalDate.of(year, month, 1);  // Ngày đầu tháng
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());  // Ngày cuối tháng

        // Lọc các tour có ngày bắt đầu hoặc kết thúc trong tháng cụ thể
        List<Tour> tours = tourService.filterToursByMonth(startOfMonth, endOfMonth);

        if (tours.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<TourDTO> filteredTourDTOs = tours.stream()
                .map(tour -> mapTourToDTO(tour))
                .collect(Collectors.toList());

        return new ResponseEntity<>(filteredTourDTOs, HttpStatus.OK);
    }


    @GetMapping("/tour-manage")
    public ResponseEntity<List<vn.edu.iuh.fit.tourmanagement.dto.tour.TourDTO>> getAllManage(){
        return ResponseEntity.ok(tourService.getAllTourManage());
    }

    @PostMapping("/upload-tour")
    public ResponseEntity<?> uploadTourImage(@RequestParam("avatar") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File không được để trống!");
            }
            String fileName = "avatars/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", fileName,
                    "overwrite", true,
                    "resource_type", "image"
            ));

            String imageUrl = (String) uploadResult.get("secure_url");

            Map<String, String> response = new HashMap<>();
            response.put("avatarUrl", imageUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Tải ảnh thất bại: " + e.getMessage());
        }
    }

}
