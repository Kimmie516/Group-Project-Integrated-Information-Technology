package sit.int221.backend.dtos.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {
    @NotNull(message = "buyerId is required")
    private Integer buyerId;

    @NotNull(message = "sellerId is required")
    private Integer sellerId;

    @NotEmpty(message = "orderItems must not be empty")
    @Valid
    private List<OrderItemRequestDto> orderItems;

    @NotNull(message = "shippingAddress is required")
    private String shippingAddress;

    private String orderNote;
}

