package sit.int221.backend.dtos.order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class PersonBriefDto {
    private Integer id;
    private String email;
    private String userType;
    private String nickName;
    private String fullName;
}
