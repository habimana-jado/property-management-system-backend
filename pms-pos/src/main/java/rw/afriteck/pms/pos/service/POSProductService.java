package rw.afriteck.pms.pos.service;

import org.springframework.web.multipart.MultipartFile;
import rw.afriteck.pms.pos.dtos.*;

import java.util.List;
import java.util.UUID;

public interface POSProductService {

    POSProductResponse create(CreatePOSProductRequest request);

    POSProductResponse update(UUID id, UpdatePOSProductRequest request);

    POSProductResponse updateImage(UUID id, MultipartFile file);

    POSProductResponse findById(UUID id);

    List<POSProductResponse> findByHotelBranch(UUID hotelBranchId);

    List<POSProductResponse> findByHotelBranchAndCategory(UUID hotelBranchId, UUID categoryId);

    List<POSProductSummaryResponse> search(UUID hotelBranchId, String query);

    List<POSProductSummaryResponse> listForBrowse(UUID hotelBranchId, UUID categoryId);

    ImageStreamResult getImageStream(UUID id);

    POSProductResponse activate(UUID id);

    POSProductResponse deactivate(UUID id);
}
