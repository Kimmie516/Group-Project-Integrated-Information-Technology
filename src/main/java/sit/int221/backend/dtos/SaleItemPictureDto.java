package sit.int221.backend.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleItemPictureDto {
    private Integer id;
    private String fileName;
    private String type;
    private Integer size;
    private Integer displayOrder;
    private String url;
}
