package sit.int221.backend.dtos.order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class OrderItemResponseDto {
    private Integer id;
    private Integer saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;
}
