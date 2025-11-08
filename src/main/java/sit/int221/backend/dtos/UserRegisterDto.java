package sit.int221.backend.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserRegisterDto {
    @NotBlank(message = "Nickname is required")
    @Size(max = 50, message = "Nickname must not exceed 50 characters")
    private String nickName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&/_])[A-Za-z\\d@$!%*?&/_]{8,}$",
            message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 4, max = 40, message = "Full name must be between 4–40 characters")
    private String fullName;

    @Pattern(regexp = "^0[0-9]{9}$", message = "Phone number must start with 0 and be 10 digits")
    private String phoneNumber;

    @Pattern(regexp = "^[0-9]{10,12}$", message = "Bank account number must be 10–12 digits")
    private String bankAccount;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Pattern(regexp = "^[0-9]{13}$", message = "ID card number must be 13 digits")
    private String idCardNumber;

    private MultipartFile idCardImageFront;
    private MultipartFile idCardImageBack;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "^(BUYER|SELLER)$", message = "User type must be BUYER or SELLER")
    private String userType;
}
