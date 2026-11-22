package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.pos.dtos.CreatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.dtos.POSProductCategoryResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.security.PosPermissions;
import rw.afriteck.pms.pos.service.POSProductCategoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos/product-categories")
@RequiredArgsConstructor
public class POSProductCategoryController {

    private final POSProductCategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductCategoryResponse create(@Valid @RequestBody CreatePOSProductCategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePOSProductCategoryRequest request) {
        return categoryService.update(id, request);
    }

    @GetMapping("/{id}")
    @PreAuthorize(PosPermissions.Has.CATALOG_VIEW)
    public POSProductCategoryResponse findById(@PathVariable UUID id) {
        return categoryService.findById(id);
    }

    @GetMapping
    @PreAuthorize(PosPermissions.Has.CATALOG_VIEW)
    public List<POSProductCategoryResponse> findByHotelBranch(@RequestParam UUID hotelBranchId) {
        return categoryService.findByHotelBranch(hotelBranchId);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductCategoryResponse activate(@PathVariable UUID id) {
        return categoryService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize(PosPermissions.Has.CATALOG_MANAGE)
    public POSProductCategoryResponse deactivate(@PathVariable UUID id) {
        return categoryService.deactivate(id);
    }
}
