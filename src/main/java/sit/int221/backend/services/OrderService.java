package sit.int221.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.order.OrderItemRequestDto;
import sit.int221.backend.dtos.order.OrderRequestDto;
import sit.int221.backend.dtos.order.OrderResponseDto;
import sit.int221.backend.entities.*;
import sit.int221.backend.mappers.OrderMapper;
import sit.int221.backend.repositories.OrderRepository;
import sit.int221.backend.repositories.SaleItemRepository; // ต้องมี
import sit.int221.backend.repositories.SellerProfileRepository;
import sit.int221.backend.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepo;
    private final SellerProfileRepository sellerProfileRepo;
    private final SaleItemRepository saleItemRepo;
    private final OrderRepository orderRepo;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto dto) {
        User buyer = userRepo.findById(dto.getBuyerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));

        SellerProfile seller = sellerProfileRepo.findById(dto.getSellerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

        List<SaleItem> saleItems = saleItemRepo.findAllById(
                dto.getOrderItems().stream().map(OrderItemRequestDto::getSaleItemId).toList()
        );

        if (saleItems.size() != dto.getOrderItems().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some sale items not found");
        }

        boolean anyWrongSeller = saleItems.stream()
                .anyMatch(si -> si.getSeller() == null || !si.getSeller().getId().equals(seller.getId()));

        if (anyWrongSeller) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All sale items must belong to the same seller");
        }

        for (OrderItemRequestDto itemDto : dto.getOrderItems()) {
            SaleItem saleItem = saleItems.stream()
                    .filter(si -> si.getProductId().equals(itemDto.getSaleItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item not found"));

            if (itemDto.getQuantity() > saleItem.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Not enough stock for " + saleItem.getModel() +
                                " (only " + saleItem.getQuantity() + " left)"
                );
            }
        }


        Order order = OrderMapper.toEntity(dto, buyer, seller, saleItems);

        Order checkedOrder = checkAndCancelOrder(order);

        if (checkedOrder.getOrderStatus() != OrderStatus.CANCELED) {
            updateStock(checkedOrder);
        }

        Order saved = orderRepo.save(checkedOrder);
        return OrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByBuyer(Integer buyerId) {
        User buyer = userRepo.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Order> orders = orderRepo.findByBuyer(buyer);

        return orders.stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")
                );

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public Order checkAndCancelOrder(Order order) {
        boolean shouldCancel = false;

        for (OrderItem orderItem : order.getOrderItems()) {
            SaleItem saleItem = orderItem.getSaleItem();
            if (saleItem == null) continue;

            int stock = saleItem.getQuantity();
            int ordered = orderItem.getQuantity();

            if (ordered > stock) {
                shouldCancel = true;
                break;
            }
        }

        if (shouldCancel) {
            order.setOrderStatus(OrderStatus.CANCELED);
            System.out.println("❌ Order ID " + order.getId() + " canceled due to insufficient stock (Rule 2.1 Logic).");
        } else if (order.getOrderStatus() == null || order.getOrderStatus() == OrderStatus.NEW) {
            order.setOrderStatus(OrderStatus.PENDING);
        }

        return order;
    }

    private void updateStock(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            SaleItem saleItem = orderItem.getSaleItem();
            int orderedQty = orderItem.getQuantity();
            int newStock = saleItem.getQuantity() - orderedQty;
            saleItem.setQuantity(newStock);
        }
    }

    @Transactional
    public OrderResponseDto cancelOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order already canceled");
        }

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completed order cannot be canceled");
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepo.save(order);

        return OrderMapper.toResponse(order);
    }
}