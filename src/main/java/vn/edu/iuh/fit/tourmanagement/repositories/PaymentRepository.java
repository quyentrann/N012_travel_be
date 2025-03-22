package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
