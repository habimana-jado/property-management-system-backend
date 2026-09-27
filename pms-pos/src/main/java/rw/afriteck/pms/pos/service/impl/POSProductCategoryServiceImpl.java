package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.pos.dtos.CreatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.dtos.POSProductCategoryResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.mapper.POSProductCategoryMapper;
import rw.afriteck.pms.pos.model.POSProductCategory;
import rw.afriteck.pms.pos.repository.POSProductCategoryRepository;
import rw.afriteck.pms.pos.service.POSProductCategoryService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class POSProductCategoryServiceImpl implements POSProductCategoryService {

    private final POSProductCategoryRepository categoryRepo;
    private final POSProductCategoryMapper mapper;

    @Override
    @Transactional
    public POSProductCategoryResponse create(CreatePOSProductCategoryRequest request) {
        if (categoryRepo.existsByHotelBranchIdAndCategoryNameIgnoreCase(request.hotelBranchId(), request.categoryName())) {
            throw new DuplicateResourceException("Product Category Name:"+ request.categoryName()+" already exists");
        }
        POSProductCategory entity = mapper.toEntity(request);
        entity.setStatus(ERecordStatus.ACTIVE);
        categoryRepo.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public POSProductCategoryResponse update(UUID id, UpdatePOSProductCategoryRequest request) {
        POSProductCategory entity = getOrThrow(id);
        if (!entity.getCategoryName().equalsIgnoreCase(request.categoryName())
                && categoryRepo.existsByHotelBranchIdAndCategoryNameIgnoreCase(entity.getHotelBranchId(), request.categoryName())) {
            throw new DuplicateResourceException("Product Category Name:"+ request.categoryName()+" already exists");
        }
        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public POSProductCategoryResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<POSProductCategoryResponse> findByHotelBranch(UUID hotelBranchId) {
        return categoryRepo.findByHotelBranchId(hotelBranchId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public POSProductCategoryResponse activate(UUID id) {
        POSProductCategory entity = getOrThrow(id);
        entity.setStatus(ERecordStatus.ACTIVE);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public POSProductCategoryResponse deactivate(UUID id) {
        POSProductCategory entity = getOrThrow(id);
        entity.setStatus(ERecordStatus.INACTIVE);
        return mapper.toResponse(entity);
    }

    private POSProductCategory getOrThrow(UUID id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("POSProductCategory", id));
    }
}
