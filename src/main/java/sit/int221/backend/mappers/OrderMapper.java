package sit.int221.backend.mappers;

import sit.int221.backend.dtos.order.*;
import sit.int221.backend.entities.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {
    public static Order toEntity(OrderRequestDto dto, User buyer, SellerProfile seller, List<SaleItem> saleItems) {
        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setShippingAddress(dto.getShippingAddress());
        order.setOrderNote(dto.getOrderNote());
        order.setOrderStatus(OrderStatus.PENDING);

        order.setOrderItems(new ArrayList<>());

        for (OrderItemRequestDto it : dto.getOrderItems()) {
            SaleItem saleItem = saleItems.stream()
                    .filter(s -> s.getProductId().equals(it.getSaleItemId()))
                    .findFirst()
                    .orElseThrow();

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setSaleItem(saleItem);
            oi.setPrice(it.getPrice());
            oi.setQuantity(it.getQuantity());
            oi.setDescription(it.getDescription());

            order.getOrderItems().add(oi);
        }

        return order;
    }

    public static OrderResponseDto toResponse(Order order) {
        PersonBriefDto sellerBrief = new PersonBriefDto(
                order.getSeller().getId(),
                order.getSeller().getUser().getEmail(),
                order.getSeller().getUser().getUserType(),
                order.getSeller().getUser().getNickName(),
                order.getSeller().getUser().getFullName()
        );

        List<OrderItemResponseDto> items = order.getOrderItems().stream()
                .map(oi -> new OrderItemResponseDto(
                        oi.getNo(),
                        oi.getSaleItem().getProductId(),
                        oi.getPrice(),
                        oi.getQuantity(),
                        oi.getDescription()
                ))
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getBuyer().getId(),
                sellerBrief,
                order.getOrderDate(),
                order.getShippingAddress(),
                order.getOrderNote(),
                items,
                order.getOrderStatus().name()
        );
    }
}
