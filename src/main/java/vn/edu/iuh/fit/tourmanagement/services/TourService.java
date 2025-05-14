package vn.edu.iuh.fit.tourmanagement.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.tourmanagement.models.Review;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Inner class thay cho record
class TourScore {
    private final Tour tour;
    private final double score;

    public TourScore(Tour tour, double score) {
        this.tour = tour;
        this.score = score;
    }

    public Tour getTour() {
        return tour;
    }

    public double getScore() {
        return score;
    }
}

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
        return tour.getReviews();
    }

    public List<Tour> getSimilarTours(Long currentTourId) {
        Tour currentTour = tourRepository.findById(currentTourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));
        return tourRepository.findSimilarTours(
                currentTourId,
                currentTour.getName(),
                currentTour.getLocation()
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
        if (price == null && location == null && !popular && startDate == null && duration == null && availableSlots == null && experienceType == null) {
            return tourRepository.findAll();
        }

        return tourRepository.findAll((Root<Tour> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (price != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), price));
            }
            if (location != null) {
                predicates.add(cb.like(root.get("location"), "%" + location + "%"));
            }
            if (popular != null && popular) {
                predicates.add(cb.greaterThan(root.get("reviews").get("rating"), 4.0));
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
        String cleanedQuery = cleanText(query.trim());
        logger.info("Normalized query: " + normalizedQuery);

        List<Tour> allTours = tourRepository.findAll();
        if (allTours.isEmpty()) {
            logger.warning("No tours found in repository");
            return List.of();
        }

        return allTours.stream()
                .map(tour -> {
                    String name = tour.getName() != null ? tour.getName() : "";
                    String location = tour.getLocation() != null ? tour.getLocation() : "";
                    String description = tour.getDescription() != null ? tour.getDescription() : "";
                    String category = tour.getTourcategory() != null && tour.getTourcategory().getCategoryName() != null ? tour.getTourcategory().getCategoryName() : "";

                    String normalizedName = normalizeString(name);
                    String normalizedLocation = normalizeString(location);
                    String normalizedDescription = normalizeString(description);
                    String normalizedCategory = normalizeString(category);

                    String cleanedName = cleanText(name);
                    String cleanedLocation = cleanText(location);
                    String cleanedDescription = cleanText(description);
                    String cleanedCategory = cleanText(category);

                    double score = 0.0;

                    // Kiểm tra khớp cụm từ đầy đủ (có dấu hoặc không dấu)
                    if (cleanedLocation.toLowerCase().contains(cleanedQuery.toLowerCase()) ||
                            normalizedLocation.contains(normalizedQuery)) {
                        score += 3.0 * 3.0;
                        logger.info("Matched full query in location: " + location + ", score: " + score);
                    }
                    if (cleanedName.toLowerCase().contains(cleanedQuery.toLowerCase()) ||
                            normalizedName.contains(normalizedQuery)) {
                        score += 1.5 * 3.0;
                        logger.info("Matched full query in name: " + name + ", score: " + score);
                    }
                    if (cleanedDescription.toLowerCase().contains(cleanedQuery.toLowerCase()) ||
                            normalizedDescription.contains(normalizedQuery)) {
                        score += 0.8 * 3.0;
                        logger.info("Matched full query in description: " + description + ", score: " + score);
                    }
                    if (cleanedCategory.toLowerCase().contains(cleanedQuery.toLowerCase()) ||
                            normalizedCategory.contains(normalizedQuery)) {
                        score += 1.0 * 3.0;
                        logger.info("Matched full query in category: " + category + ", score: " + score);
                    }

                    return new TourScore(tour, score); // Dùng inner class
                })
                .filter(tourScore -> tourScore.getScore() > 0.5)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .map(TourScore::getTour)
                .collect(Collectors.toList());
    }

    private String normalizeString(String input) {
        if (input == null) {
            return "";
        }
        String normalized = input.toLowerCase()
                .replace(",", " ")
                .replace(".", " ")
                .replace("-", " ")
                .replaceAll("[^a-zA-Z0-9\\s]", " ") // Loại bỏ ký tự đặc biệt ngoài chữ và số
                .replaceAll("\\s+", " ");
        normalized = normalized
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d");
        return normalized.trim();
    }

    private String cleanText(String input) {
        if (input == null || !(input instanceof String)) {
            return "";
        }
        return input.replaceAll("[,.]", " ").replaceAll("\\s+", " ").trim();
    }

    private String removeAccents(String text) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(text).replaceAll("");
    }

    public List<Tour> getToursByCategory(Long categoryId) {
        return tourRepository.findByTourcategory_CategoryId(categoryId);
    }

    public List<Tour> getToursByPriceRange(double minPrice, double maxPrice) {
        return tourRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Tour> searchTour(String keyword) {
        return tourRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    public List<Tour> getToursByDateRange(LocalDate startDate, LocalDate endDate) {
        // Lấy tất cả các tour
        List<Tour> tours = tourRepository.findAll();

        // Nếu startDate và endDate đều không null, thì lọc các tour theo thời gian
        if (startDate != null && endDate != null) {
            return tours.stream()
                    .filter(tour -> tour.getTourDetails().stream()
                            .anyMatch(detail -> !detail.getEndDate().isBefore(startDate) && !detail.getStartDate().isAfter(endDate)))
                    .collect(Collectors.toList());
        }

        // Nếu chỉ có một trong các tham số, xử lý theo điều kiện phù hợp
        return tours;  // Hoặc có thể lọc thêm tùy theo yêu cầu của bạn
    }


    public List<Tour> filterToursByTime(LocalDate startDate, LocalDate endDate) {
        return tourRepository.findToursByTimeRange(startDate, endDate);
    }

    public List<Tour> filterToursByDate(LocalDate filterDate) {
        return tourRepository.findByTourDetailsStartDateOrTourDetailsEndDate(filterDate, filterDate);
    }

    public List<Tour> filterToursByMonth(LocalDate startOfMonth, LocalDate endOfMonth) {
        return tourRepository.findByTourDetailsStartDateBetweenOrTourDetailsEndDateBetween(startOfMonth, endOfMonth, startOfMonth, endOfMonth);
    }


}