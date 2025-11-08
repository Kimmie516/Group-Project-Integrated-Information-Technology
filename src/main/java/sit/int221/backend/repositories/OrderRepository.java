package sit.int221.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.backend.entities.Order;
import sit.int221.backend.entities.OrderStatus;
import sit.int221.backend.entities.SellerProfile;
import sit.int221.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByBuyer(User buyer);
    Page<Order> findBySeller(SellerProfile seller, Pageable pageable);

}