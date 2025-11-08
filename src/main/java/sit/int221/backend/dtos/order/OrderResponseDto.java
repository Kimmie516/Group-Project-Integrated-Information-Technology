package sit.int221.backend.dtos.order;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor
public class OrderResponseDto {
    private Integer id;
    private Integer buyerId;
    private PersonBriefDto seller;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItemResponseDto> orderItems;
    private String orderStatus;
}
