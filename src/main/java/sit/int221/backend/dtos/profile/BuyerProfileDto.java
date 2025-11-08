package sit.int221.backend.dtos.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BuyerProfileDto implements ProfileView {
    private Integer id;
    private String role;
    private String nickname;
    private String email;
    private String fullName;
}
