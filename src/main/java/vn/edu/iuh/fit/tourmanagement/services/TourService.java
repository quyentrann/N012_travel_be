package vn.edu.iuh.fit.tourmanagement.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.client.RestTemplate;

import vn.edu.iuh.fit.tourmanagement.models.Review;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TourService {
    private static final Logger logger = Logger.getLogger(TourService.class.getName());
    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private RestTemplate restTemplate;

    public TourService(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    public Tour getTourById(Long id) {
        Optional<Tour> tour = tourRepository.findById(id);
        return tour.orElse(null);
    }
    public Optional<Tour> findById(Long id) {
        Optional<Tour> tour = tourRepository.findById(id);
        return tour;
    }


    @Transactional
    public List<Review> getTourReviews(Long tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new RuntimeException("Tour không tồn tại"));
        return tour.getReviews(); // Lúc này Hibernate mới load dữ liệu
    }


    public List<Tour> getSimilarTours(Long currentTourId) {
        // Lấy thông tin tour hiện tại
        Tour currentTour = tourRepository.findById(currentTourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));

        // Gọi repository để lấy danh sách các tour tương tự
        return tourRepository.findSimilarTours(
                currentTourId,  // Tour ID để loại trừ tour hiện tại
                currentTour.getName(),  // Tên tour hiện tại
                currentTour.getLocation()  // Địa điểm tour hiện tại
        );
    }



    public Tour createTour(Tour tour) {
        return tourRepository.save(tour);
    }

    public Tour updateTour(Tour tour) {
        return tourRepository.save(tour);
    }

    public boolean deleteTour(Long id) {
        if (!tourRepository.existsById(id)) {
            return false;
        }
        tourRepository.deleteById(id);
        return true;
    }

    public List<Tour> getToursWithFilters(Double price, String location, Boolean popular, LocalDate startDate, Integer duration, Integer availableSlots, String experienceType) {
        // Nếu không có tham số nào, trả về tất cả các tour
        if (price == null && location == null && !popular && startDate == null && duration == null && availableSlots == null && experienceType == null) {
            return tourRepository.findAll();
        }

        // Nếu có tham số, lọc tour theo các điều kiện
        return tourRepository.findAll((Root<Tour> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (price != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), price));
            }
            if (location != null) {
                predicates.add(cb.like(root.get("location"), "%" + location + "%"));
            }
            if (popular != null && popular) {
                predicates.add(cb.greaterThan(root.get("reviews").get("rating"), 4.0)); // Ví dụ: tour có rating > 4 là phổ biến
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }
            if (duration != null) {
                predicates.add(cb.equal(root.get("duration"), duration));
            }
            if (availableSlots != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("availableSlot"), availableSlots));
            }
            if (experienceType != null) {
                predicates.add(cb.like(root.get("experiences"), "%" + experienceType + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    public List<Tour> searchTours(String query) {
        if (query == null || query.trim().isEmpty()) {
            logger.info("Empty query, returning empty list");
            return List.of();
        }

        String normalizedQuery = normalizeString(query.trim());
        logger.info("Normalized query: " + normalizedQuery);

        List<Tour> allTours = tourRepository.findAll();

        return allTours.stream()
                .filter(tour -> {
                    String name = normalizeString(tour.getName());
                    String location = normalizeString(tour.getLocation());
                    String description = normalizeString(tour.getDescription());
                    String category = tour.getTourcategory() != null ? normalizeString(tour.getTourcategory().getCategoryName()) : "";

                    // Kiểm tra chuỗi con
                    boolean matches = name.contains(normalizedQuery) ||
                            location.contains(normalizedQuery) ||
                            description.contains(normalizedQuery) ||
                            category.contains(normalizedQuery);
                    if (matches) {
                        logger.info("Matched tour ID: " + tour.getTourId() + ", location: " + tour.getLocation());
                    }
                    return matches;
                })
                .collect(Collectors.toList());
    }

    private String normalizeString(String input) {
        if (input == null) {
            return "";
        }
        // Chuyển chữ thường, thay dấu phẩy bằng khoảng trắng
        String normalized = input.toLowerCase().replace(",", " ");
        // Bỏ dấu tiếng Việt
        normalized = normalized.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d");
        return normalized.trim();
    }



    private String removeAccents(String text) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(text).replaceAll("");
    }

    public List<Tour> getToursByCategory(Long categoryId) {
        return tourRepository.findByTourcategory_CategoryId(categoryId);
    }


    // Lọc tour theo giá
    public List<Tour> getToursByPriceRange(double minPrice, double maxPrice) {
        return tourRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Tour> searchTour(String keyword) {
        return tourRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }


}
