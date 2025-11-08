package sit.int221.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.int221.backend.dtos.BrandCreateDto;
import sit.int221.backend.dtos.BrandDetailDto;
import sit.int221.backend.dtos.BrandGalleryDto;
import sit.int221.backend.dtos.BrandUpdateDto;
import sit.int221.backend.services.BrandService;

import java.util.List;

@RestController
@RequestMapping("/v2/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public List<BrandGalleryDto> getAllBrands() {
        return brandService.getAllBrand();
    }

    @GetMapping("/{id}")
    public BrandDetailDto getBrandById(@PathVariable Integer id) {
        return brandService.getBrandById(id);
    }

    @PostMapping
    public ResponseEntity<BrandDetailDto> createBrand(@RequestBody @Validated BrandCreateDto brandCreateDto) {
        BrandDetailDto response = brandService.createBrand(brandCreateDto);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDetailDto> updateBrand(@PathVariable Integer id,
                                                      @RequestBody BrandUpdateDto brandUpdateDto) {
        BrandDetailDto updated = brandService.updateBrand(id, brandUpdateDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }


}
