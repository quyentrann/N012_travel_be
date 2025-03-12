package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.Discount;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
