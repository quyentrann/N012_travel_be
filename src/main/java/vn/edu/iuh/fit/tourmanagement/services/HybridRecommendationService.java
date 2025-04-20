package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.SearchHistory;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.SearchHistoryRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class HybridRecommendationService {
    private static final Logger logger = Logger.getLogger(HybridRecommendationService.class.getName());
    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    public HybridRecommendationService(TourRepository tourRepository, SearchHistoryRepository searchHistoryRepository) {
        this.tourRepository = tourRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    /**
     * Trả về danh sách tour gợi ý dựa trên lịch sử tìm kiếm của người dùng.
     */
    @Cacheable(value = "similarTours", key = "#user.id")
    public List<Tour> getRecommendations(User user) {
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            logger.warning("Invalid or non-active user, returning default recommendations");
            return tourRepository.findAll().stream().limit(5).collect(Collectors.toList());
        }

        List<SearchHistory> history = searchHistoryRepository.findTop10ByUserOrderBySearchTimeDesc(user);
        if (history.isEmpty()) {
            logger.info("No search history for user: " + user.getEmail());
            return tourRepository.findAll().stream().limit(5).collect(Collectors.toList());
        }

        List<Tour> contentBasedTours = contentBasedFiltering(history);
        List<Tour> collaborativeTours = collaborativeFiltering(user);
        List<Tour> merged = mergeRecommendations(contentBasedTours, collaborativeTours);
        logger.info("Generated " + merged.size() + " recommendations for user: " + user.getEmail());
        return merged;
    }

    /**
     * Gợi ý dựa trên nội dung tour (TF-IDF + Cosine Similarity).
     */
    private List<Tour> contentBasedFiltering(List<SearchHistory> history) {
        List<Tour> allTours = tourRepository.findAll();
        Map<Tour, Double> similarityScores = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (SearchHistory search : history) {
            long timeDiffSeconds = java.time.Duration.between(search.getSearchTime(), now).getSeconds();
            double timeWeight = 1.0 / (1.0 + timeDiffSeconds / 3600.0);

            // Ưu tiên tour đã click
            if (search.getTour() != null) {
                similarityScores.merge(search.getTour(), 5.0 * timeWeight, Double::sum); // Tăng trọng số
            }

            // Xử lý từ khóa tìm kiếm
            if (search.getSearchQuery() != null) {
                for (Tour tour : allTours) {
                    double score = 0.0;
                    score += computeSimilarity(search.getSearchQuery(), tour.getName(), 2.0);
                    score += computeSimilarity(search.getSearchQuery(), tour.getDescription(), 1.0);
                    score += computeSimilarity(search.getSearchQuery(), tour.getLocation(), 1.5);
                    if (tour.getTourcategory() != null && tour.getTourcategory().getCategoryName() != null) {
                        score += computeSimilarity(search.getSearchQuery(), tour.getTourcategory().getCategoryName(), 1.2);
                    }
                    score *= timeWeight;
                    similarityScores.merge(tour, score, Double::sum);
                }
            }
        }

        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Tour, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(15) // Tăng giới hạn để bao gồm cả tour từ từ khóa
                .collect(Collectors.toList());
    }


    //    Viết hàm giúp kiểm tra mức độ trùng khớp với từ khóa tìm kiếm:
    private double computeSimilarity(String query, String text, double weight) {
        if (query == null || text == null) return 0.0;

        String[] queryWords = query.toLowerCase().split("\\s+");
        String[] textWords = text.toLowerCase().split("\\s+");
        double matches = 0;

        for (String queryWord : queryWords) {
            for (String textWord : textWords) {
                if (textWord.contains(queryWord) || queryWord.contains(textWord)) {
                    matches += 1.0;
                }
            }
        }

        return (matches / Math.max(textWords.length, 1)) * weight;
    }


    /**
     * Tính toán mức độ tương đồng giữa từ khóa tìm kiếm và mô tả tour.
     */
    private double computeTFIDFSimilarity(List<String> searchQueries, String tourDescription) {
        if (tourDescription == null || searchQueries.isEmpty()) return 0.0;

        List<String> words = Arrays.asList(tourDescription.toLowerCase().split("\\s+"));
        Map<String, Integer> termFrequency = new HashMap<>();

        for (String word : words) {
            termFrequency.put(word, termFrequency.getOrDefault(word, 0) + 1);
        }

        double score = 0.0;
        for (String query : searchQueries) {
            if (query != null) {
                score += termFrequency.getOrDefault(query.toLowerCase(), 0);
            }
        }

        return score / words.size();
    }


    /**
     * Gợi ý dựa trên hành vi người dùng khác (Collaborative Filtering - SVD).
     */
    private List<Tour> collaborativeFiltering(User user) {
        List<SearchHistory> history = searchHistoryRepository.findTop10ByUserOrderBySearchTimeDesc(user);
        List<Long> tourIds = history.stream()
                .map(h -> h.getTour() != null ? h.getTour().getTourId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tourIds.isEmpty()) {
            logger.info("No tour clicks for user: " + user.getEmail());
            return tourRepository.findAll().stream().limit(3).collect(Collectors.toList());
        }

        try {
            List<Tour> similarTours = tourRepository.findSimilarToursBasedOnUserHistory(tourIds);
            logger.info("Found " + similarTours.size() + " collaborative tours for user: " + user.getEmail());
            return similarTours.stream().limit(5).collect(Collectors.toList());
        } catch (Exception e) {
            logger.severe("Error in collaborative filtering: " + e.getMessage());
            return tourRepository.findAll().stream().limit(3).collect(Collectors.toList());
        }
    }

    /**
     * Hợp nhất hai danh sách gợi ý và loại bỏ kết quả trùng lặp.
     */
    private List<Tour> mergeRecommendations(List<Tour> contentBased, List<Tour> collaborative) {
        Set<Tour> uniqueTours = new LinkedHashSet<>();
        uniqueTours.addAll(contentBased); // Ưu tiên content-based (click + từ khóa)
        uniqueTours.addAll(collaborative); // Thêm collaborative (chỉ từ click)
        return new ArrayList<>(uniqueTours).subList(0, Math.min(uniqueTours.size(), 15)); // Tăng giới hạn
    }

    @CacheEvict(value = "similarTours", key = "#user.id")
    public void clearCache(User user) {
        logger.info("Cleared recommendation cache for user: " + user.getEmail());
    }
}
