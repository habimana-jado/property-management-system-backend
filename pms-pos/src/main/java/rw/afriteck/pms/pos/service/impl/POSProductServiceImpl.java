package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.pos.dtos.CreatePOSProductRequest;
import rw.afriteck.pms.pos.dtos.POSProductResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductRequest;
import rw.afriteck.pms.pos.mapper.POSProductMapper;
import rw.afriteck.pms.pos.model.POSProduct;
import rw.afriteck.pms.pos.model.POSProductCategory;
import rw.afriteck.pms.pos.repository.POSProductCategoryRepository;
import rw.afriteck.pms.pos.repository.POSProductRepository;
import rw.afriteck.pms.pos.service.POSProductService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class POSProductServiceImpl implements POSProductService {

    private final POSProductRepository productRepo;
    private final POSProductCategoryRepository categoryRepo;
    private final POSProductMapper mapper;

    @Override
    @Transactional
    public POSProductResponse create(CreatePOSProductRequest request) {
        if (productRepo.existsByHotelBranchIdAndSku(request.hotelBranchId(), request.sku())) {
            throw new DuplicateResourceException("POSProduct with SKU "+ request.sku()+" already exists");
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
}
