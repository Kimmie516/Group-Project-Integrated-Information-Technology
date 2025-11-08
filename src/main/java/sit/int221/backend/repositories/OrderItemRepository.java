package sit.int221.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.backend.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {}
