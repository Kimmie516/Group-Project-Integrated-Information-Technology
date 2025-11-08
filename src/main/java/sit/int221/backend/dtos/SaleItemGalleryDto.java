package sit.int221.backend.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "model",
        "brandName",
        "description",
        "price",
        "ramGb",
        "screenSizeInch",
        "quantity",
        "storageGb",
        "color",
        "createdOn",
        "updatedOn",
        "sellerId", "sellerNickname"
})
public class SaleItemGalleryDto {
    private Integer id;
    private String model;
    private String description;
    private Integer quantity;
    private String brandName;
    private Integer price;
    private BigDecimal screenSizeInch;
    private Integer ramGb;
    private Integer storageGb;
    private String color;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private List<SaleItemPictureDto> saleItemImages;
    private Integer sellerId;
    private String sellerNickname;
}
