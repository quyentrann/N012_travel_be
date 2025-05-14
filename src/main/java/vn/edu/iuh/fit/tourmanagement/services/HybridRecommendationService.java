package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.SearchHistory;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.SearchHistoryRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HybridRecommendationService {
    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    /**
     * Trả về danh sách tour gợi ý dựa trên lịch sử tìm kiếm của người dùng.
     */
    @Cacheable(value = "similarTours", key = "#user.id")
    public List<Tour> getRecommendations(User user) {
        List<SearchHistory> history = searchHistoryRepository.findTop10ByUserOrderBySearchTimeDesc(user);
        if (history.isEmpty()) {
            // Nếu user chưa có lịch sử tìm kiếm => chỉ gợi ý những tour phổ biến (bestseller)
            return getPopularTours();
        }
        List<Tour> contentBasedTours = contentBasedFiltering(history);
        List<Tour> collaborativeTours = collaborativeFiltering(user);
        List<Tour> popularTours = getPopularTours(); // <== mới thêm nè!

        return mergeRecommendations(contentBasedTours, collaborativeTours,popularTours);
    }

    /**
     * Gợi ý dựa trên nội dung tour (TF-IDF + Cosine Similarity).
     */
    private List<Tour> contentBasedFiltering(List<SearchHistory> history) {
        List<Tour> allTours = tourRepository.findAll();
        List<String> searchQueries = history.stream()
                .map(SearchHistory::getSearchQuery)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long now = System.currentTimeMillis(); // Thời gian hiện tại
        Map<Tour, Double> similarityScores = new HashMap<>();
        for (SearchHistory search : history) {
            long searchTimeMillis = search.getSearchTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            double timeWeight = 1.0 / (1 + (now - searchTimeMillis) / 1000.0); // Trọng số thời gian

            for (Tour tour : allTours) {
                double nameScore = computeSimilarity(searchQueries, tour.getName(), 2.0);
                double descScore = computeSimilarity(searchQueries, tour.getDescription(), 1.0);
                double totalScore = (nameScore + descScore) * timeWeight; // Nhân với trọng số thời gian

                similarityScores.put(tour, similarityScores.getOrDefault(tour, 0.0) + totalScore);
            }
        }

        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Tour, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)// giới hạn tour được gợi ý
                .collect(Collectors.toList());
    }
    //    Viết hàm giúp kiểm tra mức độ trùng khớp với từ khóa tìm kiếm:
    private double computeSimilarity(List<String> searchQueries, String text, double weight) {
        if (text == null || searchQueries.isEmpty()) return 0.0;

        List<String> words = Arrays.asList(text.toLowerCase().split("\\s+"));
        Map<String, Integer> termFrequency = new HashMap<>();

        for (String word : words) {
            termFrequency.put(word, termFrequency.getOrDefault(word, 0) + 1);
        }

        double score = 0.0;
        for (String query : searchQueries) {
            score += termFrequency.getOrDefault(query, 0);
        }

        return (score / words.size()) * weight; // Chuẩn hóa theo độ dài và nhân trọng số
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
            if (query != null) { // Kiểm tra query khác null trước khi xử lý
                score += termFrequency.getOrDefault(query.toLowerCase(), 0);
            }
        }

        return score / words.size(); // Chuẩn hóa theo độ dài mô tả
    }


    /**
     * Gợi ý dựa trên hành vi người dùng khác (Collaborative Filtering - SVD).
     */
    private List<Tour> collaborativeFiltering(User user) {
        List<SearchHistory> history = searchHistoryRepository.findTop10ByUserOrderBySearchTimeDesc(user);

        List<Long> tourIds = history.stream()
                .map(h -> h.getTour() != null ? h.getTour().getTourId() : null)
                .filter(Objects::nonNull) // Loại bỏ giá trị NULL
                .collect(Collectors.toList());
        if (tourIds.isEmpty()) return Collections.emptyList(); // Không có tour nào trong lịch sử

        List<Tour> similarTours = tourRepository.findSimilarToursBasedOnUserHistory(tourIds);
        return similarTours.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * Gợi ý những tour được đặt nhiều nhất trong khoảng 30 ngày gần nhất.
     */
    private List<Tour> getPopularTours() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return tourRepository.findTopBookedToursSince(thirtyDaysAgo)
                .stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Hợp nhất hai danh sách gợi ý và loại bỏ kết quả trùng lặp.
     */
    private List<Tour> mergeRecommendations(List<Tour> contentBased, List<Tour> collaborative,List<Tour> popular) {
        if (contentBased.isEmpty()) return collaborative;
        if (collaborative.isEmpty()) return contentBased;


        Set<Tour> uniqueTours = new LinkedHashSet<>();
        uniqueTours.addAll(contentBased);
        uniqueTours.addAll(collaborative);
        uniqueTours.addAll(popular);
        return new ArrayList<>(uniqueTours);
    }
}
