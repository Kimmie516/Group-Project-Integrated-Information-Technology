package sit.int221.backend.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.backend.dtos.SaleItemCreateDto;
import sit.int221.backend.dtos.SaleItemGalleryDto;
import sit.int221.backend.dtos.SaleItemUpdateDto;
import sit.int221.backend.services.SaleItemService;
import java.util.List;

@RestController
@RequestMapping("/v1/sale-items")
public class SaleItemController {

    @Autowired
    private SaleItemService service;

    @GetMapping
    public List<SaleItemGalleryDto> getAllSaleItems() {
        return service.getAllSaleItems();
    }

    @GetMapping("/{id}")
    public SaleItemGalleryDto getSaleItemById(@PathVariable Integer id) {
        return service.getSaleItemById(id);
    }

    @PostMapping
    public ResponseEntity<SaleItemGalleryDto> createSaleItem(@RequestBody SaleItemCreateDto saleItemCreateDto) {
        SaleItemGalleryDto createdSaleItem = service.createSaleItem(saleItemCreateDto);
        return ResponseEntity.status(201).body(createdSaleItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleItemGalleryDto> updateSaleItem(
            @PathVariable Integer id,
            @RequestBody SaleItemUpdateDto dto) {
        SaleItemGalleryDto updatedItem = service.updateSaleItem(id, dto);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaleItem(@PathVariable Integer id) {
        boolean isDeleted = service.deleteSaleItem(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}

