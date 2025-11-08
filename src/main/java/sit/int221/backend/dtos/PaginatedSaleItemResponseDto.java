package sit.int221.backend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginatedSaleItemResponseDto {
    private List<SaleItemGalleryDto> content;
    private boolean last;
    private boolean first;
    private int totalPages;
    private long totalElements;
    private int size;
    private String sort;
    private int page;
}
