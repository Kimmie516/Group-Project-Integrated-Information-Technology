package sit.int221.backend.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.backend.dtos.*;
import sit.int221.backend.services.SaleItemService;

import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
public class SaleItemV2Controller {

    @Autowired
    private SaleItemService service;

    @GetMapping
    public PaginatedSaleItemResponseDto getSaleItemsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "60") int size,


            @RequestParam(name = "filterBrands", required = false) List<String> filterBrands,

            @RequestParam(name = "brands", required = false) List<String> brandsAlias,
            @RequestParam(name = "filterPriceLower", required = false) Integer legacyLower,
            @RequestParam(name = "filterPriceUpper", required = false) Integer legacyUpper,
            @RequestParam(name = "priceMin", required = false) Integer priceMin,
            @RequestParam(name = "priceMax", required = false) Integer priceMax,

            @RequestParam(name = "filterStorages", required = false) List<Integer> legacyStorages,
            @RequestParam(name = "storageSizes", required = false) List<Integer> storageSizes,

            @RequestParam(required = false) String sortField,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String direction,

            @RequestParam(name = "searchKeyWord", required = false) String searchKeyWord
    ) {
        List<String> brands = (filterBrands != null && !filterBrands.isEmpty()) ? filterBrands : brandsAlias;
        if (priceMin == null) priceMin = legacyLower;
        if (priceMax == null) priceMax = legacyUpper;
        if (storageSizes == null || storageSizes.isEmpty()) storageSizes = legacyStorages;

        String actualSort = (sortField == null || sortField.isBlank()) ? "createdOn" : sortField;
        String sortBy = actualSort.equals("brandName") ? "brand.name" : actualSort;
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(new Sort.Order(sortDirection, sortBy), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        return service.filterSaleItems(brands, priceMin, priceMax, storageSizes, searchKeyWord, pageable);
    }


    @GetMapping("/{id}")
    public ResponseEntity<SaleItemWithPicturesDto> getSaleItemById(@PathVariable Integer id) {
        var saleItem = service.findById(id);

        SaleItemWithPicturesDto dto = new SaleItemWithPicturesDto();
        dto.setId(saleItem.getProductId());
        dto.setModel(saleItem.getModel());
        dto.setBrandName(saleItem.getBrand().getName());
        dto.setDescription(saleItem.getDescription());
        dto.setPrice(saleItem.getPrice());
        dto.setRamGb(saleItem.getRamGb());
        dto.setScreenSizeInch(saleItem.getScreenSizeInch());
        dto.setQuantity(saleItem.getQuantity());
        dto.setStorageGb(saleItem.getStorageGb());
        dto.setColor(saleItem.getColor());
        dto.setCreatedOn(saleItem.getCreatedOn());
        dto.setUpdatedOn(saleItem.getUpdatedOn());
        dto.setSaleItemImages(service.findPicturesAsDto(id));

        if (saleItem.getSeller() != null) {
            dto.setSellerId(saleItem.getSeller().getId());
            dto.setSellerNickname(saleItem.getSeller().getNickName());
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/filter")
    public PaginatedSaleItemResponseDto filterSaleItems(
            @RequestParam(required = false) List<String> brands,
            @RequestParam(required = false) Integer priceMin,
            @RequestParam(required = false) Integer priceMax,
            @RequestParam(name = "filterPriceLower", required = false) Integer legacyLower,
            @RequestParam(name = "filterPriceUpper", required = false) Integer legacyUpper,

            // ✨ NEW
            @RequestParam(name = "filterStorages", required = false) List<Integer> legacyStorages,
            @RequestParam(name = "storageSizes", required = false) List<Integer> storageSizes,

            @RequestParam(required = false) String searchKeyWord,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "60") int size,
            @RequestParam(defaultValue = "createdOn") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        if (priceMin == null) priceMin = legacyLower;
        if (priceMax == null) priceMax = legacyUpper;
        if (storageSizes == null || storageSizes.isEmpty()) storageSizes = legacyStorages;

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = "brandName".equals(sortField) ? "brand.name" : sortField;
        Sort sort = Sort.by(new Sort.Order(direction, sortBy), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        return service.filterSaleItems(brands, priceMin, priceMax, storageSizes, searchKeyWord, pageable);
    }



    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SaleItemWithPicturesDto> createSaleItemWithPictures(
            @ModelAttribute SaleItemCreateDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createSaleItemWithPictures(dto, files));
    }

    @DeleteMapping("/{id}/pictures/{pictureId}")
    public ResponseEntity<String> deletePicture(
            @PathVariable Integer id,
            @PathVariable Integer pictureId
    ) {
        service.deletePicture(id, pictureId);
        return ResponseEntity.ok("Deleted");
    }

    @PutMapping(value = "/{id}/pictures/{pictureId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SaleItemPictureDto replacePicture(
            @PathVariable Integer id,
            @PathVariable Integer pictureId,
            @RequestParam("file") MultipartFile file
    ) {
        return service.replacePicture(id, pictureId, file);
    }

    @PatchMapping("/{id}/pictures/orders")
    public List<SaleItemPictureDto> updateOrders(
            @PathVariable Integer id,
            @RequestBody List<SaleItemService.OrderUpdate> updates
    ) {
        return service.updateOrders(id, updates);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SaleItemWithPicturesDto updateSaleItemWithPictures(
            @PathVariable Integer id,
            @RequestPart("saleItem") @Valid SaleItemUpdateDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> newFiles,
            @RequestParam(value = "deletePictureIds", required = false) List<Integer> deleteIds
    ) {
        return service.updateSaleItemWithPictures(id, dto, newFiles, deleteIds);
    }



}
