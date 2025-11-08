package sit.int221.backend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandDetailDto {
    private Integer id;
    private String name;
    private String websiteUrl;
    private String countryOfOrigin;
    private Boolean isActive;
    private Integer noOfSaleItems;
}
