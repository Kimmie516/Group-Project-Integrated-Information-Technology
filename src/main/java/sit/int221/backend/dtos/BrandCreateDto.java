package sit.int221.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandCreateDto {
    @NotBlank(message = "Brand name must not be blank")
    @Size(max = 30, message = "Brand name must not exceed 30 characters")
    private String name;

    @Size(max = 40, message = "Website URL must not exceed 40 characters")
    private String websiteUrl;

    @Size(max = 80, message = "Country of origin must not exceed 80 characters")
    private String countryOfOrigin;

    private Boolean isActive;

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }


}
