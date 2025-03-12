package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.Refund;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
