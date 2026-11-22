package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.security.PosPermissions;
import rw.afriteck.pms.pos.service.POSProductService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos/products")
@RequiredArgsConstructor
public class POSProductController {

    private final POSProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductResponse create(@Valid @RequestBody CreatePOSProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePOSProductRequest request) {
        return productService.update(id, request);
    }

    @GetMapping("/search")
    @PreAuthorize(PosPermissions.Has.CATALOG_VIEW)
    public List<POSProductSummaryResponse> search(@RequestParam UUID hotelBranchId, @RequestParam String q) {
        return productService.search(hotelBranchId, q);
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductResponse updateImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return productService.updateImage(id, file);
    }

    @GetMapping("/browse")
    @PreAuthorize(PosPermissions.Has.CATALOG_VIEW)
    public List<POSProductSummaryResponse> listForBrowse(@RequestParam UUID hotelBranchId,
                                                         @RequestParam(required = false) UUID categoryId) {
        return productService.listForBrowse(hotelBranchId, categoryId);
    }

    @GetMapping("/{id}/image")
    @PreAuthorize(PosPermissions.Has.CATALOG_VIEW)
    public ResponseEntity<Resource> streamImage(@PathVariable UUID id) {
        ImageStreamResult result = productService.getImageStream(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(result.resource());
    }

    @GetMapping("/{id}")
    @PreAuthorize(PosPermissions.Has.CATALOG_VIEW)
    public POSProductResponse findById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @GetMapping
    @PreAuthorize(PosPermissions.Has.CATALOG_VIEW)
    public List<POSProductResponse> find(@RequestParam UUID hotelBranchId,
                                         @RequestParam(required = false) UUID categoryId) {
        return categoryId != null
                ? productService.findByHotelBranchAndCategory(hotelBranchId, categoryId)
                : productService.findByHotelBranch(hotelBranchId);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductResponse activate(@PathVariable UUID id) {
        return productService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductResponse deactivate(@PathVariable UUID id) {
        return productService.deactivate(id);
    }
}
