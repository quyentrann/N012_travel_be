package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.SearchHistory;
import vn.edu.iuh.fit.tourmanagement.models.User;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long>{
    List<SearchHistory> findTop10ByUserOrderBySearchTimeDesc(User user);
    List<SearchHistory> findByUser(User user);
    long countByUser(User user);
    List<SearchHistory> findTopByUserOrderBySearchTimeAsc(User user, Pageable pageable);
    List<SearchHistory> findByUserAndSearchQuery(User user, String searchQuery);
}
