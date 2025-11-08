package sit.int221.backend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaleItemWithPicturesDto {
    private Integer id;
    private String model;
    private String brandName;
    private String description;
    private Integer price;
    private Integer ramGb;
    private java.math.BigDecimal screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
    private java.time.LocalDateTime createdOn;
    private java.time.LocalDateTime updatedOn;
    private List<SaleItemPictureDto> saleItemImages;
    private Integer sellerId;
    private String sellerNickname;

}
