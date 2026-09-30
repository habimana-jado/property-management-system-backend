package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.service.POSSaleService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos/sales")
@RequiredArgsConstructor
public class POSSaleController {

    private final POSSaleService saleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public POSSaleResponse create(@Valid @RequestBody CreateSaleRequest request) {
        return saleService.create(request);
    }

    @PostMapping("/{saleId}/items")
    public POSSaleResponse addItem(@PathVariable UUID saleId, @Valid @RequestBody AddSaleItemRequest request) {
        return saleService.addItem(saleId, request);
    }

    @PutMapping("/{saleId}/items/{itemId}")
    public POSSaleResponse updateItemQuantity(@PathVariable UUID saleId, @PathVariable UUID itemId,
                                              @Valid @RequestBody UpdateSaleItemQuantityRequest request) {
        return saleService.updateItemQuantity(saleId, itemId, request);
    }

    @DeleteMapping("/{saleId}/items/{itemId}")
    public POSSaleResponse removeItem(@PathVariable UUID saleId, @PathVariable UUID itemId) {
        return saleService.removeItem(saleId, itemId);
    }

    @PatchMapping("/{saleId}/hold")
    public POSSaleResponse hold(@PathVariable UUID saleId) {
        return saleService.hold(saleId);
    }

    @PatchMapping("/{saleId}/resume")
    public POSSaleResponse resume(@PathVariable UUID saleId) {
        return saleService.resume(saleId);
    }

    @PatchMapping("/{saleId}/cancel")
    public POSSaleResponse cancel(@PathVariable UUID saleId) {
        return saleService.cancel(saleId);
    }

    @PostMapping("/{saleId}/complete")
    public POSSaleResponse complete(@PathVariable UUID saleId, @Valid @RequestBody CompleteSaleRequest request) {
        return saleService.complete(saleId, request);
    }

    @GetMapping("/{saleId}")
    public POSSaleResponse findById(@PathVariable UUID saleId) {
        return saleService.findById(saleId);
    }
}
