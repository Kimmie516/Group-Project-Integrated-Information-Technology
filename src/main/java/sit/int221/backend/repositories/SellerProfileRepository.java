package sit.int221.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.backend.entities.SellerProfile;
import sit.int221.backend.entities.User;

import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Integer> {
    boolean existsByUser_Id(Integer userId);
    Optional<SellerProfile> findByUser(User user);
}
