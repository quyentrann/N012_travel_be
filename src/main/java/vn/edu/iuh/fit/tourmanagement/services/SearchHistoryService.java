package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.dto.RecommendedTourDTO;
import vn.edu.iuh.fit.tourmanagement.enums.BookingStatus;
import vn.edu.iuh.fit.tourmanagement.enums.TourStatus;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.*;
import vn.edu.iuh.fit.tourmanagement.repositories.SearchHistoryRepository;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class SearchHistoryService {

    private static final Logger logger = Logger.getLogger(SearchHistoryService.class.getName());
    private static final int MAX_SEARCH_HISTORY = 10;
    private static final int MAX_TOURS_PER_QUERY = 3; // Giảm về 3 để ưu tiên chất lượng

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
            logger.info("No valid search results for query: " + query + ", saving query without tours");
            // Vẫn lưu query để giữ lịch sử tìm kiếm
        }

        try {
            long currentCount = searchHistoryRepository.countByUser(user);
            if (currentCount >= MAX_SEARCH_HISTORY) {
                int recordsToDelete = (int) (currentCount - MAX_SEARCH_HISTORY + 1);
                List<SearchHistory> oldRecords = searchHistoryRepository.findByUser(user)
                        .stream()
                        .sorted(Comparator.comparing(SearchHistory::getSearchTime))
                        .limit(recordsToDelete)
                        .collect(Collectors.toList());
                if (!oldRecords.isEmpty()) {
                    searchHistoryRepository.deleteAll(oldRecords);
                    searchHistoryRepository.flush();
                }
            }

            String trimmedQuery = query.trim();
            if (trimmedQuery.length() > 255) {
                trimmedQuery = trimmedQuery.substring(0, 255);
            }

            SearchHistory searchHistory = SearchHistory.builder()
                    .user(user)
                    .searchQuery(trimmedQuery)
                    .tour(null) // Không lưu tour để tránh trùng với click
                    .searchTime(LocalDateTime.now())
                    .clickCount(0)
                    .build();
            searchHistoryRepository.save(searchHistory);
            logger.info("Saved search history for user ID: " + user.getId() + ", query: " + trimmedQuery);
        } catch (Exception e) {
            logger.severe("Error saving search history: " + e.getMessage());
            throw new RuntimeException("Failed to save search history", e);
        }
    }

    public List<RecommendedTourDTO> getRecommendedToursFromHistory(User user) {
        if (user == null || user.getId() == null) {
            logger.warning("User is null or has no ID, returning empty list");
            return List.of();
        }
        try {
            List<SearchHistory> histories = searchHistoryRepository.findByUser(user)
                    .stream()
                    .sorted(Comparator
                            .comparingInt(SearchHistory::getClickCount).reversed()
                            .thenComparing(SearchHistory::getSearchTime, Comparator.reverseOrder()))
                    .limit(MAX_SEARCH_HISTORY)
                    .collect(Collectors.toList());

            logger.info("Found " + histories.size() + " search history records for user ID " + user.getId());

            Map<String, List<Tour>> queryToursMap = histories.stream()
                    .filter(sh -> sh.getSearchQuery() != null)
                    .map(SearchHistory::getSearchQuery)
                    .distinct()
                    .collect(Collectors.toMap(
                            query -> query,
                            query -> {
                                List<Tour> tours = tourService.searchToursWithTFIDF(query, false).stream()
                                        .filter(tour -> tour.getStatus() == TourStatus.ACTIVE)
                                        .limit(MAX_TOURS_PER_QUERY)
                                        .collect(Collectors.toList());
                                logger.info("Query: " + query + ", found " + tours.size() + " tours");
                                return tours;
                            }
                    ));

            List<Tour> queryTours = queryToursMap.values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());

            List<Tour> clickTours = histories.stream()
                    .filter(sh -> sh.getSearchQuery() == null && sh.getTour() != null && sh.getClickCount() > 0)
                    .map(SearchHistory::getTour)
                    .filter(tour -> tour.getStatus() == TourStatus.ACTIVE)
                    .collect(Collectors.toList());

            Map<Long, Integer> tourClickCounts = histories.stream()
                    .filter(sh -> sh.getTour() != null)
                    .collect(Collectors.groupingBy(
                            sh -> sh.getTour().getTourId(),
                            Collectors.summingInt(SearchHistory::getClickCount)
                    ));

            List<Tour> allTours = new ArrayList<>();
            allTours.addAll(clickTours);
            queryTours.stream().filter(tour -> !clickTours.contains(tour)).forEach(allTours::add);

            List<TourScore> scoredTours = allTours.stream()
                    .map(tour -> {
                        double score = 0.0;
                        int clickCount = tourClickCounts.getOrDefault(tour.getTourId(), 0);
                        score += clickCount * 50.0; // Tăng trọng số click để ưu tiên tour click

                        int totalBookedPeople = tour.getBookings() != null
                                ? tour.getBookings().stream()
                                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                                .mapToInt(TourBooking::getNumberPeople)
                                .sum()
                                : 0;
                        score += totalBookedPeople * 0.5;

                        Optional<LocalDate> nearestStartDate = tour.getTourDetails().stream()
                                .map(TourDetail::getStartDate)
                                .filter(date -> !date.isBefore(LocalDate.now()))
                                .min(Comparator.naturalOrder());
                        if (nearestStartDate.isPresent()) {
                            long daysUntilStart = java.time.Duration.between(
                                    LocalDate.now().atStartOfDay(),
                                    nearestStartDate.get().atStartOfDay()
                            ).toDays();
                            score += 30.0 / (daysUntilStart + 1);
                        }

                        if (queryTours.contains(tour)) {
                            score += 30.0; // Tăng trọng số cho tour từ từ khóa
                        }

                        String query = queryToursMap.entrySet().stream()
                                .filter(e -> e.getValue().contains(tour))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null);
                        return new TourScore(tour, score, query);
                    })
                    .filter(tourScore -> tourScore.getScore() > 0.001)
                    .collect(Collectors.toList());

            scoredTours.sort(Comparator.comparingDouble(TourScore::getScore).reversed());

            List<RecommendedTourDTO> recommendedTours = new ArrayList<>();
            Set<Long> addedTourIds = new HashSet<>();
            Set<String> processedQueries = new HashSet<>();

            // Thêm tour click
            for (TourScore tourScore : scoredTours) {
                Tour tour = tourScore.getTour();
                if (clickTours.contains(tour) && !addedTourIds.contains(tour.getTourId())) {
                    recommendedTours.add(new RecommendedTourDTO(tour, "Click"));
                    addedTourIds.add(tour.getTourId());
                    logger.info("Added click tour ID: " + tour.getTourId() + ", Name: " + tour.getName() + ", Score: " + tourScore.getScore());
                }
            }

            // Thêm tour từ từ khóa
            for (String query : queryToursMap.keySet()) {
                if (processedQueries.contains(query)) continue;
                List<Tour> toursForQuery = queryToursMap.get(query);
                int count = 0;
                for (Tour tour : toursForQuery) {
                    if (!addedTourIds.contains(tour.getTourId()) && count < MAX_TOURS_PER_QUERY) {
                        recommendedTours.add(new RecommendedTourDTO(tour, query));
                        addedTourIds.add(tour.getTourId());
                        count++;
                        logger.info("Added query tour ID: " + tour.getTourId() + ", Name: " + tour.getName() + ", Source: " + query);
                    }
                    if (count >= MAX_TOURS_PER_QUERY) break;
                }
                processedQueries.add(query);
            }

            recommendedTours = recommendedTours.stream()
                    .limit(10)
                    .collect(Collectors.toList());

            logger.info("Returning " + recommendedTours.size() + " recommended tours for user ID: " + user.getId());
            recommendedTours.forEach(dto -> logger.info("Recommended tour ID: " + dto.getTour().getTourId() + ", Name: " + dto.getTour().getName() + ", Source: " + dto.getSource()));
            return recommendedTours;
        } catch (Exception e) {
            logger.severe("Error retrieving recommended tours: " + e.getMessage());
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
            Optional<SearchHistory> existingClick = searchHistoryRepository.findByUser(user).stream()
                    .filter(sh -> sh.getTour() != null && sh.getTour().getTourId().equals(tourId) && sh.getSearchQuery() == null)
                    .findFirst();

            SearchHistory searchHistory;
            if (existingClick.isPresent()) {
                searchHistory = existingClick.get();
                searchHistory.setClickCount(searchHistory.getClickCount() + 1);
                searchHistory.setSearchTime(LocalDateTime.now());
                logger.info("Incremented click count for existing history: user ID: " + user.getId() + ", tourId: " + tourId +
                        ", new clickCount: " + searchHistory.getClickCount());
            } else {
                long currentCount = searchHistoryRepository.countByUser(user);
                if (currentCount >= MAX_SEARCH_HISTORY) {
                    List<Long> toDeleteIds = searchHistoryRepository.findTopByUserOrderBySearchTimeAsc(user, PageRequest.of(0, 1))
                            .stream()
                            .map(SearchHistory::getId)
                            .collect(Collectors.toList());
                    if (!toDeleteIds.isEmpty()) {
                        logger.info("Deleted " + toDeleteIds.size() + " old search history records for user ID: " + user.getId());
                        searchHistoryRepository.deleteAllById(toDeleteIds);
                    }
                }

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