package sit.int221.backend.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private Integer id;
    private String email;
    private String displayName;
    private String role;
    private String accessToken;
    private String refreshToken;
}
