package rw.afriteck.pms.pos.service;

import rw.afriteck.pms.pos.dtos.CreatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.dtos.POSProductCategoryResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface POSProductCategoryService {

    POSProductCategoryResponse create(CreatePOSProductCategoryRequest request);

    POSProductCategoryResponse update(UUID id, UpdatePOSProductCategoryRequest request);

    POSProductCategoryResponse findById(UUID id);

    List<POSProductCategoryResponse> findByHotelBranch(UUID hotelBranchId);

    POSProductCategoryResponse activate(UUID id);

    POSProductCategoryResponse deactivate(UUID id);
}
