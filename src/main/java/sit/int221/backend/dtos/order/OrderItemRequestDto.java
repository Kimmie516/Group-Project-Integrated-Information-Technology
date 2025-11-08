package sit.int221.backend.dtos.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequestDto {
    @NotNull(message = "saleItemId is required")
    private Integer saleItemId;

    @NotNull @Min(value = 1, message = "price must be >= 1")
    private Integer price;

    @NotNull @Min(value = 1, message = "quantity must be >= 1")
    private Integer quantity;

    private String description;
}
