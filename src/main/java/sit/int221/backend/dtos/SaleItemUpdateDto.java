package sit.int221.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaleItemUpdateDto {
    @NotBlank
    private String model;

    @NotNull
    private BrandGalleryDto brand;

    @NotBlank
    private String description;

    @NotNull
    private Integer price;

    @NotNull
    private Integer storageGb;

    @NotBlank
    private String color;

    @NotNull
    private Integer quantity;

    private Integer ramGb;
    private BigDecimal screenSizeInch;
}
