package sit.int221.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellerProfileDto {
    private String nickName;
    private String fullName;
    private String email;
    private String bankAccount;
    private String bankName;
    private String idCardNumber;
    private String idCardImageFront;
    private String idCardImageBack;
}

