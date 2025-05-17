package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.SearchHistory;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.SearchHistoryRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchHistoryService {

    private static final Logger logger = Logger.getLogger(SearchHistoryService.class.getName());
    private static final int MAX_SEARCH_HISTORY = 10;
    private static final int MAX_TOURS_PER_QUERY = 3;

    private final SearchHistoryRepository searchHistoryRepository;
    private final TourRepository tourRepository;
    private final TourService tourService;

    public SearchHistoryService(
            SearchHistoryRepository searchHistoryRepository,
            TourRepository tourRepository,
            TourService tourService) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.tourRepository = tourRepository;
        this.tourService = tourService;
    }

    @Transactional
    public void saveSearch(User user, String query, List<Tour> searchResults) {
        if (user == null || query == null || query.trim().isEmpty()) {
            logger.warning("Invalid data: user or query is null/empty");
            return;
        }
        if (user.getId() == null) {
            logger.warning("User ID is null, cannot save search history");
            return;
        }
        if (user.getStatus() == null || user.getStatus() != UserStatus.ACTIVE) {
            logger.info("Skipping search history save for non-active user: " + (user.getEmail() != null ? user.getEmail() : "ID=" + user.getId()));
            return;
        }
        if (searchResults == null || searchResults.isEmpty()) {
            logger.info("No valid search results for query: " + query + ", skipping save search history");
            return;
        }

        try {
            // Đếm số bản ghi hiện tại
            long currentCount = searchHistoryRepository.countByUser(user);
            logger.info("Current search history count for user ID " + user.getId() + ": " + currentCount);

            // Chỉ lưu 1 bản ghi với searchQuery (không lưu tour)
            if (currentCount >= MAX_SEARCH_HISTORY) {
                int recordsToDelete = (int) (currentCount - MAX_SEARCH_HISTORY + 1);
                logger.info("Need to delete " + recordsToDelete + " old records");

                List<SearchHistory> oldRecords = searchHistoryRepository.findByUser(user)
                        .stream()
                        .sorted(Comparator.comparing(SearchHistory::getSearchTime))
                        .limit(recordsToDelete)
                        .collect(Collectors.toList());

                if (!oldRecords.isEmpty()) {
                    logger.info("Deleting old search history records: " + oldRecords.size());
                    searchHistoryRepository.deleteAll(oldRecords);
                    searchHistoryRepository.flush();
                    long countAfterDelete = searchHistoryRepository.countByUser(user);
                    logger.info("After deletion, new count for user ID " + user.getId() + ": " + countAfterDelete);
                }
            }

            // Cắt ngắn query nếu quá dài
            String trimmedQuery = query.trim();
            if (trimmedQuery.length() > 255) {
                trimmedQuery = trimmedQuery.substring(0, 255);
                logger.info("Truncated search query to 255 characters: " + trimmedQuery);
            }

            // Lưu bản ghi chỉ với searchQuery
            SearchHistory searchHistory = SearchHistory.builder()
                    .user(user)
                    .searchQuery(trimmedQuery)
                    .tour(null) // Không lưu tour
                    .searchTime(LocalDateTime.now())
                    .clickCount(0)
                    .build();
            searchHistoryRepository.save(searchHistory);
            logger.info("Saved search history for user ID: " + user.getId() + ", query: " + trimmedQuery);

            long finalCount = searchHistoryRepository.countByUser(user);
            logger.info("Final search history count for user ID " + user.getId() + ": " + finalCount);
        } catch (Exception e) {
            logger.severe("Error saving search history for user ID: " + user.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to save search history: " + e.getMessage(), e);
        }
    }

    public List<Tour> getRecommendedToursFromHistory(User user) {
        if (user == null || user.getId() == null) {
            logger.warning("User is null or has no ID, returning empty list");
            return List.of();
        }
        try {
            // Lấy tất cả search_history của user
            List<SearchHistory> histories = searchHistoryRepository.findByUser(user)
                    .stream()
                    .sorted(Comparator
                            .comparingInt(SearchHistory::getClickCount).reversed()
                            .thenComparing(SearchHistory::getSearchTime, Comparator.reverseOrder()))
                    .limit(MAX_SEARCH_HISTORY)
                    .collect(Collectors.toList());

            logger.info("Found " + histories.size() + " search history records for user ID " + user.getId());

            // Lấy tour từ search_query, tối đa 3 tour mỗi query
            List<Tour> queryTours = histories.stream()
                    .filter(sh -> sh.getSearchQuery() != null)
                    .map(SearchHistory::getSearchQuery)
                    .distinct()
                    .flatMap(query -> {
                        logger.info("Fetching tours for query: " + query);
                        return tourService.searchTours(query).stream()
                                .filter(tour -> tour.getStatus() == TourStatus.ACTIVE &&
                                        tour.getAvailableSlot() > 0 &&
                                        tour.getTourDetails().stream().anyMatch(detail ->
                                                !detail.getStartDate().isBefore(LocalDate.now())))
                                .limit(MAX_TOURS_PER_QUERY); // Giới hạn 3 tour mỗi query
                    })
                    .collect(Collectors.toList());

            logger.info("Found " + queryTours.size() + " tours from search queries");

            // Lấy tour từ click (search_query = null, tour != null, click_count > 0)
            List<Tour> clickTours = histories.stream()
                    .filter(sh -> sh.getSearchQuery() == null && sh.getTour() != null && sh.getClickCount() > 0)
                    .map(SearchHistory::getTour)
                    .filter(tour -> tour.getStatus() == TourStatus.ACTIVE &&
                            tour.getAvailableSlot() > 0 &&
                            tour.getTourDetails().stream().anyMatch(detail ->
                                    !detail.getStartDate().isBefore(LocalDate.now())))
                    .collect(Collectors.toList());

            logger.info("Found " + clickTours.size() + " tours from clicks");

            // Tính tổng click_count cho mỗi tour
            Map<Long, Integer> tourClickCounts = histories.stream()
                    .filter(sh -> sh.getTour() != null)
                    .collect(Collectors.groupingBy(
                            sh -> sh.getTour().getTourId(),
                            Collectors.summingInt(SearchHistory::getClickCount)
                    ));

            // Hợp danh sách và sắp xếp theo click_count
            List<Tour> recommendedTours = Stream.concat(clickTours.stream(), queryTours.stream())
                    .distinct()
                    .sorted((t1, t2) -> {
                        int clicks1 = tourClickCounts.getOrDefault(t1.getTourId(), 0);
                        int clicks2 = tourClickCounts.getOrDefault(t2.getTourId(), 0);
                        return Integer.compare(clicks2, clicks1); // Tour có click_count cao xếp trước
                    })
                    .collect(Collectors.toList());

            logger.info("Returning " + recommendedTours.size() + " recommended tours for user ID " + user.getId());
            recommendedTours.forEach(tour -> logger.info("Recommended tour ID: " + tour.getTourId() + ", Name: " + tour.getName()));
            return recommendedTours;
        } catch (Exception e) {
            logger.severe("Error retrieving recommended tours for user ID: " + user.getId() + ": " + e.getMessage());
            return List.of();
        }
    }

    public List<SearchHistory> getUserSearchHistory(User user) {
        if (user == null || user.getId() == null) {
            logger.warning("User is null or has no ID, returning empty list");
            return List.of();
        }
        try {
            List<SearchHistory> history = searchHistoryRepository.findByUser(user);
            // Sắp xếp: clickCount giảm dần, sau đó searchTime giảm dần cho clickCount = 0
            return history.stream()
                    .sorted(Comparator
                            .comparingInt(SearchHistory::getClickCount).reversed()
                            .thenComparing(sh -> sh.getClickCount() == 0 ? sh.getSearchTime() : LocalDateTime.MIN, Comparator.reverseOrder()))
                    .limit(MAX_SEARCH_HISTORY)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.severe("Error retrieving search history for user ID: " + user.getId() + ": " + e.getMessage());
            return List.of();
        }
    }

    @Transactional
    public void updateTourClick(User user, Long tourId) {
        if (user == null || tourId == null) {
            logger.warning("Invalid data: user or tourId is null");
            return;
        }
        if (user.getId() == null) {
            logger.warning("User ID is null, cannot update tour click");
            return;
        }
        if (user.getStatus() == null || user.getStatus() != UserStatus.ACTIVE) {
            logger.info("Skipping tour click update for non-active user: " + (user.getEmail() != null ? user.getEmail() : "ID=" + user.getId()));
            return;
        }

        Optional<Tour> tourOptional = tourRepository.findById(tourId);
        if (tourOptional.isEmpty()) {
            logger.warning("Tour not found: " + tourId);
            return;
        }

        Tour tour = tourOptional.get();

        try {
            // Kiểm tra xem có bản ghi click với tour này và searchQuery = null chưa
            Optional<SearchHistory> existingClick = searchHistoryRepository.findByUser(user).stream()
                    .filter(sh -> sh.getTour() != null && sh.getTour().getTourId().equals(tourId) && sh.getSearchQuery() == null)
                    .findFirst();

            SearchHistory searchHistory;
            if (existingClick.isPresent()) {
                // Tăng clickCount
                searchHistory = existingClick.get();
                searchHistory.setClickCount(searchHistory.getClickCount() + 1);
                searchHistory.setSearchTime(LocalDateTime.now()); // Cập nhật thời gian
                logger.info("Incremented click count for existing history: user ID: " + user.getId() + ", tourId: " + tourId +
                        ", new clickCount: " + searchHistory.getClickCount());
            } else {
                // Đếm số bản ghi hiện tại
                long currentCount = searchHistoryRepository.countByUser(user);
                if (currentCount >= MAX_SEARCH_HISTORY) {
                    // Xóa bản ghi cũ nhất
                    List<Long> toDeleteIds = searchHistoryRepository.findTopByUserOrderBySearchTimeAsc(user, PageRequest.of(0, 1))
                            .stream()
                            .map(SearchHistory::getId)
                            .collect(Collectors.toList());
                    if (!toDeleteIds.isEmpty()) {
                        searchHistoryRepository.deleteAllById(toDeleteIds);
                        logger.info("Deleted " + toDeleteIds.size() + " old search history records for user ID: " + user.getId());
                    }
                }

                // Tạo bản ghi mới
                searchHistory = SearchHistory.builder()
                        .user(user)
                        .searchQuery(null)
                        .tour(tour)
                        .searchTime(LocalDateTime.now())
                        .clickCount(1)
                        .build();
                logger.info("Created new search history for click: user ID: " + user.getId() + ", tourId: " + tourId);
            }

            searchHistoryRepository.save(searchHistory);
        } catch (Exception e) {
            logger.severe("Error updating tour click for user ID: " + user.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to update tour click: " + e.getMessage(), e);
        }
    }

    public List<SearchHistory> getSearchHistoryByTour(User user, Long tourId) {
        if (user == null || tourId == null) {
            logger.warning("Invalid data: user or tourId is null");
            return List.of();
        }
        return searchHistoryRepository.findByUser(user).stream()
                .filter(sh -> sh.getTour() != null && sh.getTour().getTourId().equals(tourId))
                .collect(Collectors.toList());
    }

    public void deleteOldSearchHistory(User user, LocalDateTime before) {
        if (user == null || before == null) {
            logger.warning("Invalid data: user or before is null");
            return;
        }
        List<SearchHistory> oldSearches = searchHistoryRepository.findByUser(user).stream()
                .filter(sh -> sh.getSearchTime().isBefore(before))
                .collect(Collectors.toList());
        searchHistoryRepository.deleteAll(oldSearches);
        logger.info("Deleted old search history for user ID: " + (user.getId() != null ? user.getId() : "unknown"));
    }
}