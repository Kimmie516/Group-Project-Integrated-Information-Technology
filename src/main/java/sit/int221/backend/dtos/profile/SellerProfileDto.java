package sit.int221.backend.dtos.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SellerProfileDto implements ProfileView {
    private Integer id;
    private String role;
    private String nickname;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String bankAccount;
    private String bankName;
    private Boolean bankProvided;
}
