package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.pos.dtos.CreatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.dtos.POSProductCategoryResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductCategoryRequest;
import rw.afriteck.pms.pos.service.POSProductCategoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos-product-categories")
@RequiredArgsConstructor
public class POSProductCategoryController {

    private final POSProductCategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public POSProductCategoryResponse create(@Valid @RequestBody CreatePOSProductCategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public POSProductCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePOSProductCategoryRequest request) {
        return categoryService.update(id, request);
    }

    @GetMapping("/{id}")
    public POSProductCategoryResponse findById(@PathVariable UUID id) {
        return categoryService.findById(id);
    }

    @GetMapping
    public List<POSProductCategoryResponse> findByHotelBranch(@RequestParam UUID hotelBranchId) {
        return categoryService.findByHotelBranch(hotelBranchId);
    }

    @PatchMapping("/{id}/activate")
    public POSProductCategoryResponse activate(@PathVariable UUID id) {
        return categoryService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public POSProductCategoryResponse deactivate(@PathVariable UUID id) {
        return categoryService.deactivate(id);
    }
}
