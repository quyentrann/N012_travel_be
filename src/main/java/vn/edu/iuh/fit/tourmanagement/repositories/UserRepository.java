package vn.edu.iuh.fit.tourmanagement.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.tourmanagement.enums.UserStatus;
import vn.edu.iuh.fit.tourmanagement.models.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndPassword(String email, String password);
    @Query("SELECT u FROM User u WHERE u.email = :email")
@EntityGraph(attributePaths = "customer")
Optional<User> findByEmail(@Param("email") String email);

    // Kiểm tra sự tồn tại của người dùng theo email
    boolean existsByEmail(String email);

    // Bạn có thể thêm các truy vấn khác theo yêu cầu, ví dụ:
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = :status")
    Optional<User> findByEmailAndStatus(@Param("email") String email, @Param("status") UserStatus status);

}