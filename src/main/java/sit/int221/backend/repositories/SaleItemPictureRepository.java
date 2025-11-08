package sit.int221.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.backend.entities.SaleItemPicture;

import java.util.List;
import java.util.Optional;

public interface SaleItemPictureRepository extends JpaRepository<SaleItemPicture, Integer> {
    int countBySaleItem_ProductId(Integer saleItemId);

    List<SaleItemPicture> findBySaleItem_ProductIdOrderByDisplayOrderAsc(Integer saleItemId);

    Optional<SaleItemPicture> findByIdAndSaleItem_ProductId(Integer id, Integer saleItemId);
}
