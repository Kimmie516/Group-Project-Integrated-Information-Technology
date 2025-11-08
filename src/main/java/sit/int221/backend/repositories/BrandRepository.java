package sit.int221.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.backend.entities.Brand;
import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findByNameIgnoreCase(String name);
    List<Brand> findAllByOrderByIdAsc();
}
