package vn.edu.iuh.fit.tourmanagement.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.tourmanagement.dto.tour.TourDTO;
import vn.edu.iuh.fit.tourmanagement.dto.tour.TourRequest;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;
import vn.edu.iuh.fit.tourmanagement.models.Review;
import vn.edu.iuh.fit.tourmanagement.models.SearchHistory;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TourService {
    private static final Logger logger = Logger.getLogger(TourService.class.getName());
    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

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

    public List<Tour> searchTours(String query, boolean random) {
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

        Map<Long, Integer> tourClickCounts = searchHistoryRepository.findAll().stream()
                .filter(sh -> sh.getTour() != null)
                .collect(Collectors.groupingBy(
                        sh -> sh.getTour().getTourId(),
                        Collectors.summingInt(SearchHistory::getClickCount)
                ));

        List<TourScore> scoredTours = allTours.stream()
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

                    if (cleanedLocation.toLowerCase().contains(cleanedQuery.toLowerCase()) ||
                            normalizedLocation.contains(normalizedQuery)) {
                        score += 3.0;
                    }
                    if (cleanedName.toLowerCase().contains(cleanedQuery.toLowerCase()) ||
                            normalizedName.contains(normalizedQuery)) {
                        score += 1.5;
                    }
                    if (cleanedDescription.toLowerCase().contains(cleanedQuery.toLowerCase()) ||
                            normalizedDescription.contains(normalizedQuery)) {
                        score += 0.8;
                    }
                    if (cleanedCategory.toLowerCase().contains(cleanedQuery.toLowerCase()) ||
                            normalizedCategory.contains(normalizedQuery)) {
                        score += 1.0;
                    }

                    int clickCount = tourClickCounts.getOrDefault(tour.getTourId(), 0);
                    score += clickCount * 0.5;

                    Optional<LocalDate> nearestStartDate = tour.getTourDetails().stream()
                            .map(TourDetail::getStartDate)
                            .filter(date -> !date.isBefore(LocalDate.now()))
                            .min(Comparator.naturalOrder());
                    if (nearestStartDate.isPresent()) {
                        long daysUntilStart = java.time.Duration.between(LocalDate.now().atStartOfDay(), nearestStartDate.get().atStartOfDay()).toDays();
                        score += (30.0 / (daysUntilStart + 1));
                    }

                    return new TourScore(tour, score, query);
                })
                .filter(tourScore -> tourScore.getScore() > 0.5)
                .collect(Collectors.toList());

        if (random) {
            Collections.shuffle(scoredTours);
            return scoredTours.stream()
                    .map(TourScore::getTour)
                    .limit(10)
                    .collect(Collectors.toList());
        }

        return scoredTours.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .map(TourScore::getTour)
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<Tour> searchToursWithTFIDF(String query, boolean random) {
        if (query == null || query.trim().isEmpty()) {
            logger.info("Empty query, returning empty list");
            return List.of();
        }

        String originalQuery = query.trim().toLowerCase();
        String normalizedQuery = normalizeString(originalQuery);
        logger.info("Original query: " + originalQuery + ", Normalized query for TF-IDF: " + normalizedQuery);

        List<Tour> allTours = tourRepository.findAll();
        if (allTours.isEmpty()) {
            logger.warning("No tours found in repository");
            return List.of();
        }

        Map<String, Integer> docFrequency = new HashMap<>();
        Map<Tour, Map<String, Double>> tfVectors = new HashMap<>();

        for (Tour tour : allTours) {
            String text = (tour.getName() != null ? tour.getName() : "") + " " +
                    (tour.getDescription() != null ? tour.getDescription() : "") + " " +
                    (tour.getLocation() != null ? tour.getLocation() : "") + " " +
                    (tour.getTourcategory() != null && tour.getTourcategory().getCategoryName() != null ? tour.getTourcategory().getCategoryName() : "");
            text = normalizeString(text.toLowerCase());
            String[] words = text.split("\\s+");
            Map<String, Integer> termFreq = new HashMap<>();
            for (String word : words) {
                if (!word.isEmpty()) {
                    termFreq.put(word, termFreq.getOrDefault(word, 0) + 1);
                    docFrequency.put(word, docFrequency.getOrDefault(word, 0) + 1);
                }
            }

            Map<String, Double> tfVector = new HashMap<>();
            for (String word : termFreq.keySet()) {
                double tf = (double) termFreq.get(word) / Math.max(words.length, 1);
                tfVector.put(word, tf);
            }
            tfVectors.put(tour, tfVector);
        }

        String[] queryWords = normalizedQuery.split("\\s+");
        Map<String, Double> queryVector = new HashMap<>();
        Map<String, Integer> queryFreq = new HashMap<>();
        for (String word : queryWords) {
            if (!word.isEmpty()) queryFreq.put(word, queryFreq.getOrDefault(word, 0) + 1);
        }
        for (String word : queryFreq.keySet()) {
            double tf = (double) queryFreq.get(word) / Math.max(queryWords.length, 1);
            double idf = Math.log((double) allTours.size() / (docFrequency.getOrDefault(word, 1) + 1));
            queryVector.put(word, tf * idf);
        }

        Map<Long, Integer> tourClickCounts = searchHistoryRepository.findAll().stream()
                .filter(sh -> sh.getTour() != null)
                .collect(Collectors.groupingBy(sh -> sh.getTour().getTourId(), Collectors.summingInt(SearchHistory::getClickCount)));

        List<TourScore> scores = new ArrayList<>();
        for (Tour tour : allTours) {
            Map<String, Double> tourVector = tfVectors.get(tour);
            double dotProduct = 0.0, normQuery = 0.0, normTour = 0.0;

            for (String word : queryVector.keySet()) {
                double queryTfIdf = queryVector.get(word);
                double tourTf = tourVector.getOrDefault(word, 0.0);
                dotProduct += queryTfIdf * tourTf;
                normQuery += queryTfIdf * queryTfIdf;
                normTour += tourTf * tourTf;
            }

            double similarity = dotProduct / (Math.sqrt(normQuery) * Math.sqrt(normTour) + 1e-10);
            double score = similarity * 30.0;

            int clickCount = tourClickCounts.getOrDefault(tour.getTourId(), 0);
            score += clickCount * 0.5;

            Optional<LocalDate> nearestStartDate = tour.getTourDetails().stream()
                    .map(TourDetail::getStartDate)
                    .filter(date -> !date.isBefore(LocalDate.now()))
                    .min(Comparator.naturalOrder());
            if (nearestStartDate.isPresent()) {
                long daysUntilStart = java.time.Duration.between(LocalDate.now().atStartOfDay(), nearestStartDate.get().atStartOfDay()).toDays();
                score += 30.0 / (daysUntilStart + 1);
            }

            String tourText = (tour.getName() != null ? tour.getName().toLowerCase() : "") + " " +
                    (tour.getDescription() != null ? tour.getDescription().toLowerCase() : "") + " " +
                    (tour.getLocation() != null ? tour.getLocation().toLowerCase() : "");
            if (tourText.contains(originalQuery)) {
                score += 5.0;
                logger.info("Tour ID: " + tour.getTourId() + " matches original query: " + originalQuery);
            }
            if (tour.getLocation() != null && tour.getLocation().toLowerCase().contains(originalQuery)) {
                score += 10.0;
                logger.info("Tour ID: " + tour.getTourId() + " matches location: " + originalQuery);
            }
            if (tour.getName() != null && tour.getName().toLowerCase().contains(originalQuery)) {
                score += 7.0;
                logger.info("Tour ID: " + tour.getTourId() + " matches name: " + originalQuery);
            }

            logger.info("Tour ID: " + tour.getTourId() + ", Name: " + tour.getName() + ", TF-IDF Score: " + score);
            scores.add(new TourScore(tour, score, originalQuery));
        }

        List<TourScore> filteredScores = scores.stream()
                .filter(s -> s.getScore() > 0.0001)
                .collect(Collectors.toList());

        if (filteredScores.isEmpty()) {
            logger.warning("No tours passed score threshold for query: " + originalQuery);
        }

        if (random) Collections.shuffle(filteredScores);
        else filteredScores.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        List<Tour> result = filteredScores.stream()
                .map(TourScore::getTour)
                .limit(10)
                .collect(Collectors.toList());
        logger.info("Returning " + result.size() + " tours for query: " + normalizedQuery);
        result.forEach(tour -> logger.info("Result Tour ID: " + tour.getTourId() + ", Name: " + tour.getName()));
        return result;
    }

    private String normalizeString(String input) {
        if (input == null) return "";
        // Chuẩn hóa dấu tiếng Việt
        String normalized = Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", ""); // Xóa dấu thanh
        normalized = normalized
                .replace("đ", "d")
                .replaceAll("[^a-z0-9\\s]", " ") // Xóa ký tự đặc biệt, giữ chữ và số
                .replaceAll("\\s+", " ") // Chuẩn hóa khoảng trắng
                .trim();
        return normalized;
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
        List<Tour> tours = tourRepository.findAll();
        if (startDate != null && endDate != null) {
            return tours.stream()
                    .filter(tour -> tour.getTourDetails().stream()
                            .anyMatch(detail -> !detail.getEndDate().isBefore(startDate) && !detail.getStartDate().isAfter(endDate)))
                    .collect(Collectors.toList());
        }
        return tours;
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

    public List<TourDTO> getAllTourManage(){
        List<Tour> tours = tourRepository.findAll();
        List<TourDTO> tourRequests = tours.stream().map(tour->{
            return new TourDTO(
                    tour.getTourId(),
                    tour.getName(),
                    tour.getLocation(),
                    tour.getPrice(),
                    tour.getAvailableSlot(),
                    tour.getTourcategory(),
                    tour.getDescription(),
                    tour.getStatus(),
                    tour.getImageURL()
            );
        }).toList();
        return tourRequests;
    }

    @Autowired
    TourCategoryRepository tourCategoryRepository;

    public Tour addTour(TourRequest tourRequest) {
        Tour tour = new Tour();
        tour.setName(tourRequest.getName());
        tour.setLocation(tourRequest.getLocation());
        tour.setPrice(tourRequest.getPrice());
        tour.setAvailableSlot(tourRequest.getAvailableSlot());
        tour.setTourcategory(tourCategoryRepository.findById(tourRequest.getTourcategoryId()).get());
        tour.setDescription(tourRequest.getDescription());
        tour.setStatus(TourStatus.valueOf(tourRequest.getStatus().trim()));
        tour.setImageURL(tourRequest.getImageURL());

        Tour savedTour = tourRepository.save(tour);
        return savedTour;
    }
}