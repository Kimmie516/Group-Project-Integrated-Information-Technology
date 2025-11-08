package sit.int221.backend.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleItemResponseDto {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private Integer storage;
    private String brandName;
}
