package sit.int221.backend.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaleItemBySellerCreateDto {
    @NotNull
    private Integer brandId;

    @NotBlank
    @Size(min = 1, max = 60)
    private String model;

    @NotBlank
    @Size(min = 1, max = 16384)
    private String description;

    @NotNull
    @Min(0)
    private Integer price;

    @NotNull
    @Min(1)
    private Integer storageGb;

    @NotBlank
    @Size(max = 40)
    private String color;

    @NotNull
    @Min(0)
    private Integer quantity;

    @NotNull
    @Min(1)
    private Integer ramGb;

    // ทศนิยมไม่เกิน 2 ตำแหน่ง
    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 2, fraction = 2)
    private BigDecimal screenSizeInch;
}
