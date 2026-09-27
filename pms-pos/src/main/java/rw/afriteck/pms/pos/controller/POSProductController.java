package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.pos.dtos.CreatePOSProductRequest;
import rw.afriteck.pms.pos.dtos.POSProductResponse;
import rw.afriteck.pms.pos.dtos.UpdatePOSProductRequest;
import rw.afriteck.pms.pos.service.POSProductService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos-products")
@RequiredArgsConstructor
public class POSProductController {

    private final POSProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public POSProductResponse create(@Valid @RequestBody CreatePOSProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public POSProductResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePOSProductRequest request) {
        return productService.update(id, request);
    }

    @GetMapping("/{id}")
    public POSProductResponse findById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @GetMapping
    public List<POSProductResponse> find(@RequestParam UUID hotelBranchId,
                                         @RequestParam(required = false) UUID categoryId) {
        return categoryId != null
                ? productService.findByHotelBranchAndCategory(hotelBranchId, categoryId)
                : productService.findByHotelBranch(hotelBranchId);
    }

    @PatchMapping("/{id}/activate")
    public POSProductResponse activate(@PathVariable UUID id) {
        return productService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public POSProductResponse deactivate(@PathVariable UUID id) {
        return productService.deactivate(id);
    }
}
