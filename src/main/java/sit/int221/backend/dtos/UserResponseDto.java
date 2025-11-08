package sit.int221.backend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    private Integer id;
    private String nickName;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Boolean isActive;
    private String userType;
}
