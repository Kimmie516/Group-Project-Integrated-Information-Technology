package sit.int221.backend.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sit.int221.backend.dtos.SaleItemBySellerCreateDto;
import sit.int221.backend.dtos.SaleItemGalleryDto;
import sit.int221.backend.entities.User;
import sit.int221.backend.exceptions.ApiException;
import sit.int221.backend.services.SellerSaleItemService;

import java.util.List;

@RestController
@RequestMapping("/v2/sellers")
public class SellerSaleItemController {

    @Autowired
    private SellerSaleItemService sellerSaleItemService;

    @GetMapping("/{sellerId}/sale-items")
    public ResponseEntity<List<SaleItemGalleryDto>> getSaleItemsBySeller(
            @PathVariable Integer sellerId,
            @AuthenticationPrincipal User loginUser
    ) {
        if (loginUser == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized: Please login first");
        }

        List<SaleItemGalleryDto> items = sellerSaleItemService.getItemsBySeller(sellerId, loginUser.getId());
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{sellerId}/sale-items")
    public ResponseEntity<SaleItemGalleryDto> addSaleItem(
            @PathVariable Integer sellerId,
            @Valid @RequestBody SaleItemBySellerCreateDto dto,
            @AuthenticationPrincipal User loginUser
    ) {
        if (loginUser == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized: Please login first");
        }

        SaleItemGalleryDto newItem = sellerSaleItemService.createSaleItem(sellerId, dto, loginUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newItem);
    }

    @DeleteMapping("/{sellerId}/sale-items/{saleItemId}")
    public ResponseEntity<Void> deleteSaleItem(
            @PathVariable Integer sellerId,
            @PathVariable Integer saleItemId,
            @AuthenticationPrincipal User loginUser
    ) {
        if (loginUser == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized: Please login first");
        }

        sellerSaleItemService.deleteSaleItemBySeller(sellerId, saleItemId, loginUser.getId());
        return ResponseEntity.noContent().build();
    }


}
