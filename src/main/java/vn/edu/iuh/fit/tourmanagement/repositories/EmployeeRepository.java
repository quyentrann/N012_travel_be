package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.tourmanagement.models.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
