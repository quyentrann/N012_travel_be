package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.SearchHistory;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.User;
import vn.edu.iuh.fit.tourmanagement.repositories.SearchHistoryRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class SearchHistoryService {

    private static final Logger logger = Logger.getLogger(SearchHistoryService.class.getName());
    private static final int MAX_SEARCH_HISTORY = 10;

    private final SearchHistoryRepository searchHistoryRepository;
    private final TourRepository tourRepository;

    public SearchHistoryService(SearchHistoryRepository searchHistoryRepository, TourRepository tourRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.tourRepository = tourRepository;
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
            if (currentCount >= MAX_SEARCH_HISTORY) {
                // Lấy ID của các bản ghi cũ nhất để xóa
                List<Long> toDeleteIds = searchHistoryRepository.findTopByUserOrderBySearchTimeAsc(user, PageRequest.of(0, (int) (currentCount - MAX_SEARCH_HISTORY + 1)))
                        .stream()
                        .map(SearchHistory::getId)
                        .collect(Collectors.toList());
                if (!toDeleteIds.isEmpty()) {
                    searchHistoryRepository.deleteAllById(toDeleteIds);
                    logger.info("Deleted " + toDeleteIds.size() + " old search history records for user ID: " + user.getId());
                }
            }

            // Kiểm tra và lấy tour hợp lệ
            Tour matchedTour = searchResults.get(0);
            if (matchedTour.getTourId() == null || !tourRepository.existsById(matchedTour.getTourId())) {
                logger.warning("Invalid tour ID: " + (matchedTour.getTourId() != null ? matchedTour.getTourId() : "null") + ", skipping save search history");
                return;
            }

            // Cắt ngắn query nếu quá dài
            String trimmedQuery = query.trim();
            if (trimmedQuery.length() > 255) {
                trimmedQuery = trimmedQuery.substring(0, 255);
                logger.info("Truncated search query to 255 characters: " + trimmedQuery);
            }

            SearchHistory searchHistory = SearchHistory.builder()
                    .user(user)
                    .searchQuery(trimmedQuery)
                    .tour(matchedTour)
                    .searchTime(LocalDateTime.now())
                    .clickCount(0)
                    .build();
            searchHistoryRepository.save(searchHistory);
            logger.info("Saved search history for user ID: " + user.getId() + ", query: " + trimmedQuery +
                    ", tour ID: " + matchedTour.getTourId());
        } catch (Exception e) {
            logger.severe("Error saving search history for user ID: " + user.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to save search history: " + e.getMessage(), e);
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