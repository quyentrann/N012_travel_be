package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.TourSchedule;

public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
}
