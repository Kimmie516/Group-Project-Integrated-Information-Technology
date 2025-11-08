package sit.int221.backend.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.backend.dtos.*;
import sit.int221.backend.entities.Brand;
import sit.int221.backend.entities.SaleItem;
import sit.int221.backend.entities.SaleItemPicture;
import sit.int221.backend.repositories.SaleItemRepository;
import sit.int221.backend.repositories.SaleItemPictureRepository;
import sit.int221.backend.specifications.SaleItemSpecifications;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class    SaleItemService {

    private static final int MAX_PICTURES = 4;
    private static final long MAX_FILE_BYTES = 2 * 1024 * 1024L; // 2MB

    @Autowired
    private SaleItemRepository repository;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SaleItemPictureRepository pictureRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager em;

    @Value("${app.upload.base-dir:${APP_UPLOAD_BASE_DIR:/data/uploads}}")
    private String baseUploadDir;

    @Value("${app.upload.public-base-url:${APP_UPLOAD_PUBLIC_BASE_URL:/media}}")
    private String publicBaseUrl;

    public List<SaleItemGalleryDto> getAllSaleItems() {
        List<SaleItem> products = repository.findAllByOrderByProductIdAsc();
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public SaleItemGalleryDto getSaleItemById(Integer id) {
        SaleItem product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "SalesItem not found for this id :: " + id
                ));
        return convertToDto(product);
    }

    public SaleItemGalleryDto createSaleItem(SaleItemCreateDto saleItemCreateDto) {
        SaleItem saleItem = new SaleItem();

        String model = saleItemCreateDto.getModel() != null ? saleItemCreateDto.getModel().trim() : null;
        String description = saleItemCreateDto.getDescription() != null ? saleItemCreateDto.getDescription().trim() : null;
        String color = processColor(saleItemCreateDto.getColor());

        if (model == null || model.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model must not be null or empty.");
        }
        if (model.length() > 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model cannot exceed 60 characters.");
        }
        if (description == null || description.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must not be null or empty.");
        }

        Integer inputQuantity = saleItemCreateDto.getQuantity();
        int quantity = (inputQuantity == null || inputQuantity < 0) ? 1 : inputQuantity;

        saleItem.setModel(model);
        saleItem.setDescription(description);
        saleItem.setPrice(saleItemCreateDto.getPrice());
        saleItem.setRamGb(saleItemCreateDto.getRamGb());
        saleItem.setScreenSizeInch(saleItemCreateDto.getScreenSizeInch());
        saleItem.setQuantity(quantity);
        saleItem.setStorageGb(saleItemCreateDto.getStorageGb());
        saleItem.setColor(color);
        saleItem.setCreatedOn(LocalDateTime.now());
        saleItem.setUpdatedOn(LocalDateTime.now());

        Brand brand;
        if (saleItemCreateDto.getBrand().getId() != null) {
            brand = brandService.findBrandById(saleItemCreateDto.getBrand().getId());
        } else {
            brand = brandService.findOrCreateBrandByName(saleItemCreateDto.getBrand().getName());
        }
        saleItem.setBrand(brand);

        SaleItem savedSaleItem = repository.save(saleItem);
        return convertToDto(savedSaleItem);
    }

    public SaleItemGalleryDto updateSaleItem(Integer id, SaleItemUpdateDto dto) {
        SaleItem existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item not found for ID: " + id));

        String model = dto.getModel() != null ? dto.getModel().trim() : null;
        String description = dto.getDescription() != null ? dto.getDescription().trim() : null;
        String color = processColor(dto.getColor());

        if (model == null || model.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model must not be null or empty.");
        }
        if (model.length() > 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model cannot exceed 60 characters.");
        }
        if (description == null || description.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must not be null or empty.");
        }

        Integer quantity = dto.getQuantity();
        if (quantity == null || quantity < 0) {
            quantity = 1;
        }

        existing.setModel(model);
        existing.setDescription(description);
        existing.setPrice(dto.getPrice());
        existing.setRamGb(dto.getRamGb());
        existing.setScreenSizeInch(dto.getScreenSizeInch());
        existing.setQuantity(quantity);
        existing.setStorageGb(dto.getStorageGb());
        existing.setColor(color);
        existing.setUpdatedOn(LocalDateTime.now());

        Brand brand;
        if (dto.getBrand().getId() != null) {
            brand = brandService.findBrandById(dto.getBrand().getId());
        } else {
            brand = brandService.findOrCreateBrandByName(dto.getBrand().getName());
        }
        existing.setBrand(brand);

        repository.save(existing);
        return convertToDto(existing);
    }

    public boolean deleteSaleItem(Integer id) {
        SaleItem saleItem = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Sale item not found for ID: " + id
                ));
        repository.delete(saleItem);
        return true;
    }

    public PaginatedSaleItemResponseDto getSaleItemsPaginatedResponse(List<String> brands, Pageable pageable) {
        Page<SaleItem> page;
        if (brands != null && !brands.isEmpty()) {
            List<String> cleanedBrands = brands.stream()
                    .filter(b -> b != null && !b.isBlank())
                    .map(String::trim)
                    .toList();
            page = cleanedBrands.isEmpty()
                    ? repository.findAll(pageable)
                    : repository.findByBrand_NameInIgnoreCase(cleanedBrands, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        List<SaleItemGalleryDto> content = page.getContent().stream()
                .map(this::convertToDto)
                .toList();

        PaginatedSaleItemResponseDto response = new PaginatedSaleItemResponseDto();
        response.setContent(content);
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        Sort.Order mainSort = pageable.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        String sortString = mainSort.getProperty() + ": " + mainSort.getDirection().name();
        response.setSort(sortString);



        return response;
    }

    public PaginatedSaleItemResponseDto filterSaleItems(
            List<String> brands, Integer priceMin, Integer priceMax,
            List<Integer> storageSizes, String searchKeyWord, Pageable pageable
    ) {
        if (priceMin != null && priceMax != null && priceMin > priceMax) {
            PaginatedSaleItemResponseDto empty = new PaginatedSaleItemResponseDto();
            empty.setContent(List.of());
            empty.setPage(pageable.getPageNumber());
            empty.setSize(pageable.getPageSize());
            empty.setTotalElements(0);
            empty.setTotalPages(0);
            empty.setFirst(true);
            empty.setLast(true);
            empty.setSort(pageable.getSort().toString());
            return empty;
        }

        Specification<SaleItem> spec = Specification
                .where(SaleItemSpecifications.brandIn(brands))
                .and(SaleItemSpecifications.priceBetween(priceMin, priceMax))
                .and(SaleItemSpecifications.storageIn(storageSizes));

        if (searchKeyWord != null && !searchKeyWord.isBlank()) {
            spec = spec.and(SaleItemSpecifications.searchByKeyword(searchKeyWord));
        }

        Page<SaleItem> page = repository.findAll(spec, pageable);

        List<SaleItemGalleryDto> content = page.getContent().stream()
                .map(this::convertToDto)
                .toList();

        PaginatedSaleItemResponseDto response = new PaginatedSaleItemResponseDto();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setSize(page.getSize());

        Sort.Order mainSort = pageable.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        response.setSort(mainSort.getProperty() + ": " + mainSort.getDirection().name());

        return response;
    }


    private PaginatedSaleItemResponseDto emptyPageResponse(Pageable pageable) {
        PaginatedSaleItemResponseDto response = new PaginatedSaleItemResponseDto();
        response.setContent(List.of());
        response.setPage(pageable.getPageNumber());
        response.setTotalPages(0);
        response.setTotalElements(0);
        response.setFirst(true);
        response.setLast(true);
        response.setSize(pageable.getPageSize());
        response.setSort("id: ASC");
        return response;
    }

    public SaleItemWithPicturesDto createSaleItemWithPictures(SaleItemCreateDto dto, List<MultipartFile> files) {
        SaleItem saleItem = new SaleItem();
        saleItem.setModel(dto.getModel().trim());
        saleItem.setDescription(dto.getDescription().trim());
        saleItem.setPrice(dto.getPrice());
        saleItem.setQuantity(dto.getQuantity() == null ? 1 : dto.getQuantity());
        saleItem.setStorageGb(dto.getStorageGb());
        saleItem.setColor(processColor(dto.getColor()));
        saleItem.setCreatedOn(LocalDateTime.now());
        saleItem.setUpdatedOn(LocalDateTime.now());

        Brand brand = dto.getBrand().getId() != null
                ? brandService.findBrandById(dto.getBrand().getId())
                : brandService.findOrCreateBrandByName(dto.getBrand().getName());
        saleItem.setBrand(brand);

        SaleItem savedSaleItem = repository.save(saleItem);

        if (files != null && !files.isEmpty()) {
            if (files.size() > MAX_PICTURES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 4 pictures are allowed.");
            }
            savePictures(savedSaleItem, files, 1);
        }

        SaleItemWithPicturesDto result = new SaleItemWithPicturesDto();
        result.setId(savedSaleItem.getProductId());
        result.setModel(savedSaleItem.getModel());
        result.setBrandName(savedSaleItem.getBrand().getName());
        result.setDescription(savedSaleItem.getDescription());
        result.setPrice(savedSaleItem.getPrice());
        result.setRamGb(savedSaleItem.getRamGb());
        result.setScreenSizeInch(savedSaleItem.getScreenSizeInch());
        result.setQuantity(savedSaleItem.getQuantity());
        result.setStorageGb(savedSaleItem.getStorageGb());
        result.setColor(savedSaleItem.getColor());
        result.setCreatedOn(savedSaleItem.getCreatedOn());
        result.setUpdatedOn(savedSaleItem.getUpdatedOn());
        result.setSaleItemImages(findPicturesAsDto(savedSaleItem.getProductId()));

        return result;
    }


    public void deletePicture(Integer saleItemId, Integer pictureId) {
        SaleItemPicture pic = pictureRepository.findByIdAndSaleItem_ProductId(pictureId, saleItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Picture not found"));

        try {
            Files.deleteIfExists(Paths.get(pic.getPath()));
        } catch (IOException ignore) {}

        pictureRepository.delete(pic);


        List<SaleItemPicture> remain = pictureRepository.findBySaleItem_ProductIdOrderByDisplayOrderAsc(saleItemId);
        int i = 1;
        for (SaleItemPicture p : remain) {
            if (!p.getDisplayOrder().equals(i)) {
                p.setDisplayOrder(i);
                pictureRepository.save(p);
            }
            i++;
        }
    }


    @Transactional
    public SaleItemPictureDto replacePicture(Integer saleItemId, Integer pictureId, MultipartFile file) {
        SaleItemPicture pic = pictureRepository.findByIdAndSaleItem_ProductId(pictureId, saleItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Picture not found"));

        validateImageFile(file);

        Path oldPath = Paths.get(pic.getPath());
        Path dir = oldPath.getParent();
        String newName = UUID.randomUUID() + "_" +
                (file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        Path newPath = dir.resolve(newName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, newPath, StandardCopyOption.REPLACE_EXISTING);
            try { Files.deleteIfExists(oldPath); } catch (IOException ignore) {}
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Replace failed: " + e.getMessage(), e);
        }

        pic.setFileName(newName);
        pic.setPath(newPath.toString());
        pic.setType(file.getContentType());
        pic.setSize((int) file.getSize());
        pictureRepository.save(pic);

        return toDto(pic, saleItemId);
    }
    @Transactional
    public List<SaleItemPictureDto> updateOrders(Integer saleItemId, List<OrderUpdate> updates) {
        List<SaleItemPicture> pics = pictureRepository.findBySaleItem_ProductIdOrderByDisplayOrderAsc(saleItemId);
        int n = pics.size();
        if (n == 0) return List.of();

        var expected = pics.stream().map(SaleItemPicture::getId).collect(java.util.stream.Collectors.toSet());
        var got = updates.stream().map(OrderUpdate::pictureId).collect(java.util.stream.Collectors.toSet());
        if (got.size() != n || !got.equals(expected)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid picture ids");
        }

        var desired = new java.util.HashMap<Integer, Integer>();
        for (OrderUpdate u : updates) desired.put(u.pictureId(), u.displayOrder());

        jdbcTemplate.update(
                "UPDATE sale_item_pictures SET display_order = display_order + 1000 WHERE sale_item_id = ?",
                saleItemId
        );

        StringBuilder sql = new StringBuilder("UPDATE sale_item_pictures SET display_order = CASE id ");
        java.util.List<Object> args = new java.util.ArrayList<>();
        for (SaleItemPicture p : pics) {
            sql.append(" WHEN ? THEN ? ");
            args.add(p.getId());
            args.add(desired.get(p.getId()));
        }
        sql.append(" END WHERE sale_item_id = ?");
        args.add(saleItemId);

        jdbcTemplate.update(sql.toString(), args.toArray());


        return findPicturesAsDto(saleItemId);
    }

    public SaleItemWithPicturesDto updateSaleItemWithPictures(Integer id,
                                                              SaleItemUpdateDto dto,
                                                              List<MultipartFile> newFiles,
                                                              List<Integer> deleteIds) {
        SaleItem existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item not found"));

        existing.setModel(dto.getModel().trim());
        existing.setDescription(dto.getDescription().trim());
        existing.setPrice(dto.getPrice());
        existing.setRamGb(dto.getRamGb());
        existing.setScreenSizeInch(dto.getScreenSizeInch());
        existing.setQuantity(dto.getQuantity() == null ? 1 : dto.getQuantity());
        existing.setStorageGb(dto.getStorageGb());
        existing.setColor(processColor(dto.getColor()));
        existing.setUpdatedOn(LocalDateTime.now());

        Brand brand = dto.getBrand().getId() != null
                ? brandService.findBrandById(dto.getBrand().getId())
                : brandService.findOrCreateBrandByName(dto.getBrand().getName());
        existing.setBrand(brand);

        repository.save(existing);

        if (deleteIds != null) {
            for (Integer pid : deleteIds) {
                deletePicture(id, pid);
            }
        }


        if (newFiles != null && !newFiles.isEmpty()) {
            int current = pictureRepository.countBySaleItem_ProductId(id);
            if (current + newFiles.size() > MAX_PICTURES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Max 4 pictures. Existing: " + current + ", new: " + newFiles.size());
            }
            savePictures(existing, newFiles, current + 1);
        }

        SaleItemWithPicturesDto result = new SaleItemWithPicturesDto();
        result.setId(existing.getProductId());
        result.setModel(existing.getModel());
        result.setBrandName(existing.getBrand().getName());
        result.setDescription(existing.getDescription());
        result.setPrice(existing.getPrice());
        result.setRamGb(existing.getRamGb());
        result.setScreenSizeInch(existing.getScreenSizeInch());
        result.setQuantity(existing.getQuantity());
        result.setStorageGb(existing.getStorageGb());
        result.setColor(existing.getColor());
        result.setCreatedOn(existing.getCreatedOn());
        result.setUpdatedOn(existing.getUpdatedOn());

        result.setSaleItemImages(findPicturesAsDto(id));

        return result;
    }




    private void savePictures(SaleItem saleItem, List<MultipartFile> files, int startOrder) {
        Path destDir = Paths.get(baseUploadDir)
                .resolve("sale-items")
                .resolve(String.valueOf(saleItem.getProductId()))
                .toAbsolutePath();
        try {
            Files.createDirectories(destDir);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot create upload directory", e);
        }

        int order = startOrder;
        for (MultipartFile file : files) {
            validateImageFile(file);

            String fileName = UUID.randomUUID() + "_" +
                    (file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
            Path target = destDir.resolve(fileName);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed", e);
            }

            SaleItemPicture picture = new SaleItemPicture();
            picture.setSaleItem(saleItem);
            picture.setFileName(fileName);
            picture.setPath(target.toString());
            picture.setType(file.getContentType());
            picture.setSize((int) file.getSize());
            picture.setDisplayOrder(order++);
            pictureRepository.save(picture);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
        String ct = file.getContentType();
        if (ct == null || !ct.toLowerCase().startsWith("image/"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        if (file.getSize() > MAX_FILE_BYTES)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each file must be <= 2 MB");
    }

    private SaleItemGalleryDto convertToDto(SaleItem item) {
        SaleItemGalleryDto dto = new SaleItemGalleryDto();
        dto.setId(item.getProductId());
        dto.setModel(item.getModel());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setRamGb(item.getRamGb());
        dto.setScreenSizeInch(item.getScreenSizeInch());
        dto.setQuantity(item.getQuantity());
        dto.setStorageGb(item.getStorageGb());
        dto.setColor(item.getColor());
        dto.setBrandName(item.getBrand().getName());
        dto.setCreatedOn(item.getCreatedOn());
        dto.setUpdatedOn(item.getUpdatedOn());

        dto.setSaleItemImages(findPicturesAsDto(item.getProductId()));

        if (item.getSeller() != null) {
            dto.setSellerId(item.getSeller().getId());
            dto.setSellerNickname(item.getSeller().getNickName());
        }
        return dto;
    }

    private String processColor(String color) {
        if (color == null) return null;
        color = color.trim();
        return color.isEmpty() ? null : color;
    }

    private SaleItemPictureDto toDto(SaleItemPicture p, Integer saleItemId) {
        String url = "/media/sale-items/" + p.getSaleItem().getProductId() + "/" + p.getFileName();

        return SaleItemPictureDto.builder()
                .id(p.getId())
                .fileName(p.getFileName())
                .type(p.getType())
                .size(p.getSize())
                .displayOrder(p.getDisplayOrder())
                .url(url)
                .build();
    }



    @Transactional(readOnly = true)
    public List<SaleItemPictureDto> findPicturesAsDto(Integer saleItemId) {
        return pictureRepository.findBySaleItem_ProductIdOrderByDisplayOrderAsc(saleItemId)
                .stream()
                .map(p -> toDto(p, saleItemId))
                .toList();
    }

    public SaleItem findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Sale item not found for id: " + id
                ));
    }



    public record OrderUpdate(Integer pictureId, Integer displayOrder) {}

}