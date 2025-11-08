package sit.int221.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.order.OrderResponseDto;
import sit.int221.backend.entities.Order;
import sit.int221.backend.entities.SellerProfile;
import sit.int221.backend.mappers.OrderMapper;
import sit.int221.backend.repositories.OrderRepository;
import sit.int221.backend.repositories.SellerProfileRepository;

@Service
@RequiredArgsConstructor
public class SellerOrderService {
    private final OrderRepository orderRepo;
    private final SellerProfileRepository sellerRepo;

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersBySeller(Integer sellerId, Pageable pageable) {
        SellerProfile seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

        Page<Order> ordersPage = orderRepo.findBySeller(seller, pageable);

        return ordersPage.map(OrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderBySellerAndId(Integer sellerId, Integer orderId) {
        SellerProfile seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order does not belong to this seller");
        }

        return OrderMapper.toResponse(order);
    }
}
