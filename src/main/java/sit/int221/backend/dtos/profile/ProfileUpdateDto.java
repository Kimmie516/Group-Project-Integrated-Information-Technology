package sit.int221.backend.dtos.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDto {
    @NotBlank
    @Size(max = 50)
    @JsonProperty("nickname")
    private String nickName;

    @NotBlank
    @Size(max = 100)
    @JsonProperty("fullname")
    private String fullName;
}
