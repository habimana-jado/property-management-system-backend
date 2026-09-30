package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.mapper.POSProductMapper;
import rw.afriteck.pms.pos.model.POSProduct;
import rw.afriteck.pms.pos.model.POSProductCategory;
import rw.afriteck.pms.pos.repository.POSProductCategoryRepository;
import rw.afriteck.pms.pos.repository.POSProductRepository;
import rw.afriteck.pms.pos.service.POSProductService;
import rw.afriteck.pms.pos.service.ProductImageStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class POSProductServiceImpl implements POSProductService {

    private final POSProductRepository productRepo;
    private final POSProductCategoryRepository categoryRepo;
    private final POSProductMapper mapper;
    private final ProductImageStorage imageStorage;

    @Override
    @Transactional
    public POSProductResponse create(CreatePOSProductRequest request) {
        if (productRepo.existsByHotelBranchIdAndSku(request.hotelBranchId(), request.sku())) {
            throw new DuplicateResourceException("POSProduct with SKU "+ request.sku()+" already exists");
        }
        if (request.barcode() != null
                && productRepo.existsByHotelBranchIdAndBarcode(request.hotelBranchId(), request.barcode())) {
            throw new DuplicateResourceException("POSProduct with barcode"+ request.barcode()+" already exists");
        }
        POSProductCategory category = categoryRepo.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("POSProductCategory", request.categoryId()));

        POSProduct entity = mapper.toEntity(request);
        entity.setCategory(category);
        entity.setStatus(ERecordStatus.ACTIVE);
        productRepo.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public POSProductResponse updateImage(UUID id, MultipartFile file) {
        POSProduct entity = getOrThrow(id);
        String storedPath = imageStorage.store(id, file);
        entity.setImageUrl(storedPath);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public POSProductResponse update(UUID id, UpdatePOSProductRequest request) {
        POSProduct entity = getOrThrow(id);
        POSProductCategory category = categoryRepo.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("POSProductCategory", request.categoryId()));

        mapper.updateEntity(entity, request);
        entity.setCategory(category);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<POSProductSummaryResponse> listForBrowse(UUID hotelBranchId, UUID categoryId) {
        List<POSProduct> products = categoryId != null
                ? productRepo.findByHotelBranchIdAndCategoryIdAndStatus(hotelBranchId, categoryId, ERecordStatus.ACTIVE)
                : productRepo.findByHotelBranchIdAndStatus(hotelBranchId, ERecordStatus.ACTIVE);

        return products.stream()
                .map(mapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ImageStreamResult getImageStream(UUID id) {
        POSProduct entity = getOrThrow(id);
        if (entity.getImageUrl() == null) {
            throw new ResourceNotFoundException("Product image", id);
        }

        Path imagePath = Path.of(entity.getImageUrl());
        if (!Files.exists(imagePath)) {
            throw new ResourceNotFoundException("Product image", id);
        }

        Resource resource = new FileSystemResource(imagePath);
        String contentType = probeContentType(imagePath);

        return new ImageStreamResult(resource, contentType);
    }

    @Override
    @Transactional(readOnly = true)
    public POSProductResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<POSProductResponse> findByHotelBranch(UUID hotelBranchId) {
        return productRepo.findByHotelBranchId(hotelBranchId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<POSProductResponse> findByHotelBranchAndCategory(UUID hotelBranchId, UUID categoryId) {
        return productRepo.findByHotelBranchIdAndCategoryId(hotelBranchId, categoryId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public POSProductResponse activate(UUID id) {
        POSProduct entity = getOrThrow(id);
        entity.setStatus(ERecordStatus.ACTIVE);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public POSProductResponse deactivate(UUID id) {
        POSProduct entity = getOrThrow(id);
        entity.setStatus(ERecordStatus.INACTIVE);
        return mapper.toResponse(entity);
    }

    private POSProduct getOrThrow(UUID id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("POSProduct", id));
    }

    private String probeContentType(Path imagePath) {
        try {
            String detected = Files.probeContentType(imagePath);
            return detected != null ? detected : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
