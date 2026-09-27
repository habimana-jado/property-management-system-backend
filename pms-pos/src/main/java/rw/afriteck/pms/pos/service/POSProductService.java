package rw.afriteck.pms.pos.service;

import rw.afriteck.pms.pos.dtos.CreatePOSProductRequest;
import rw.afriteck.pms.pos.dtos.POSProductResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductRequest;

import java.util.List;
import java.util.UUID;

public interface POSProductService {

    POSProductResponse create(CreatePOSProductRequest request);

    POSProductResponse update(UUID id, UpdatePOSProductRequest request);

    POSProductResponse findById(UUID id);

    List<POSProductResponse> findByHotelBranch(UUID hotelBranchId);

    List<POSProductResponse> findByHotelBranchAndCategory(UUID hotelBranchId, UUID categoryId);

    POSProductResponse activate(UUID id);

    POSProductResponse deactivate(UUID id);
}
