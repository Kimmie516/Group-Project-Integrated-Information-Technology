package sit.int221.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.BrandCreateDto;
import sit.int221.backend.dtos.BrandDetailDto;
import sit.int221.backend.dtos.BrandGalleryDto;
import sit.int221.backend.dtos.BrandUpdateDto;
import sit.int221.backend.entities.Brand;
import sit.int221.backend.repositories.BrandRepository;
import sit.int221.backend.repositories.SaleItemRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    public Brand findOrCreateBrandByName(String brandName) {
        return brandRepository.findByNameIgnoreCase(brandName)
                .orElseGet(() -> {
                    Brand newBrand = new Brand();
                    newBrand.setName(brandName);
                    return brandRepository.save(newBrand);
                });
    }

    public Brand findBrandById(Integer id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Brand not found with id: " + id
                ));
    }

    public List<BrandGalleryDto> getAllBrand() {
        List<Brand> brands = brandRepository.findAllByOrderByIdAsc();
        return brands.stream().map(brand -> {
            BrandGalleryDto dto = new BrandGalleryDto();
            dto.setId(brand.getId());
            dto.setName(brand.getName());
            int count = brand.getSaleItems().size();
            dto.setNoOfSaleItems(count);

            return dto;
        }).collect(Collectors.toList());
    }

    public BrandDetailDto getBrandById(Integer id) {
        Brand brand = findBrandById(id);

        BrandDetailDto dto = new BrandDetailDto();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setCountryOfOrigin(brand.getCountryOfOrigin());
        dto.setWebsiteUrl(brand.getWebSiteUrl());
        dto.setIsActive(brand.getIsActive());

        int count = brand.getSaleItems().size();
        dto.setNoOfSaleItems(count);

        return dto;
    }

    public BrandDetailDto createBrand(BrandCreateDto brandDto) {
        if (brandRepository.findByNameIgnoreCase(brandDto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate name");
        }

        try {
            Brand brand = new Brand();
            brand.setName(brandDto.getName());
            brand.setCountryOfOrigin(brandDto.getCountryOfOrigin());
            brand.setWebSiteUrl(brandDto.getWebsiteUrl());
            brand.setIsActive(brandDto.getIsActive() != null ? brandDto.getIsActive() : false);


            Brand saved = brandRepository.save(brand);

            BrandDetailDto response = new BrandDetailDto();
            response.setId(saved.getId());
            response.setName(saved.getName());
            response.setCountryOfOrigin(saved.getCountryOfOrigin());
            response.setWebsiteUrl(brand.getWebSiteUrl());
            response.setIsActive(saved.getIsActive() != null ? saved.getIsActive() : false);
            response.setNoOfSaleItems(0);

            return response;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Create failed");
        }
    }

    public BrandDetailDto updateBrand(Integer id, BrandUpdateDto brandDto) {
        Brand brand = findBrandById(id);

        String newName        = brandDto.getName() != null ? brandDto.getName().trim()           : null;
        String newWebsiteUrl  = brandDto.getWebsiteUrl() != null ? brandDto.getWebsiteUrl().trim() : null;
        String newCountry     = brandDto.getCountryOfOrigin() != null ? brandDto.getCountryOfOrigin().trim() : null;
        Boolean newIsActive   = brandDto.getIsActive() != null ? brandDto.getIsActive() : false;

        brandRepository.findByNameIgnoreCase(newName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate name");
            }
        });

        brand.setName(newName);
        brand.setWebSiteUrl(newWebsiteUrl);
        brand.setCountryOfOrigin(newCountry);
        brand.setIsActive(newIsActive);

        Brand updated = brandRepository.save(brand);

        BrandDetailDto response = new BrandDetailDto();
        response.setId(updated.getId());
        response.setName(updated.getName());
        response.setWebsiteUrl(updated.getWebSiteUrl());
        response.setCountryOfOrigin(updated.getCountryOfOrigin());
        response.setIsActive(updated.getIsActive());

        int count = updated.getSaleItems().size();
        response.setNoOfSaleItems(count);

        return response;
    }


    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found"));

        if (saleItemRepository.existsByBrand_Id(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Delete " + brand.getName() + " is not allowed. There are sale items with " + brand.getName() + " brand.");
        }

        brandRepository.deleteById(id);
    }

}
