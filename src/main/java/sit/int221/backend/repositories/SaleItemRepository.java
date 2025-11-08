package sit.int221.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sit.int221.backend.entities.SaleItem;

import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer>, JpaSpecificationExecutor<SaleItem> {
    List<SaleItem> findAllByOrderByProductIdAsc();

    List<SaleItem> findBySeller_Id(Integer sellerId);

    boolean existsByBrand_Id(Integer brandId);

    Page<SaleItem> findByBrand_NameInIgnoreCase(List<String> brandNames, Pageable pageable);
}

