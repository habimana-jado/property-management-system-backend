package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.security.PosPermissions;
import rw.afriteck.pms.pos.service.POSSaleService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos/sales")
@RequiredArgsConstructor
public class POSSaleController {

    private final POSSaleService saleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PosPermissions.Has.SALE_CREATE)
    public POSSaleResponse create(@Valid @RequestBody CreateSaleRequest request) {
        return saleService.create(request);
    }

    @PostMapping("/{saleId}/items")
    @PreAuthorize(PosPermissions.Has.SALE_CREATE)
    public POSSaleResponse addItem(@PathVariable UUID saleId, @Valid @RequestBody AddSaleItemRequest request) {
        return saleService.addItem(saleId, request);
    }

    @PutMapping("/{saleId}/items/{itemId}")
    @PreAuthorize(PosPermissions.Has.SALE_CREATE)
    public POSSaleResponse updateItemQuantity(@PathVariable UUID saleId, @PathVariable UUID itemId,
                                              @Valid @RequestBody UpdateSaleItemQuantityRequest request) {
        return saleService.updateItemQuantity(saleId, itemId, request);
    }

    @DeleteMapping("/{saleId}/items/{itemId}")
    @PreAuthorize(PosPermissions.Has.SALE_CREATE)
    public POSSaleResponse removeItem(@PathVariable UUID saleId, @PathVariable UUID itemId) {
        return saleService.removeItem(saleId, itemId);
    }

    @PatchMapping("/{saleId}/hold")
    @PreAuthorize(PosPermissions.Has.SALE_CREATE)
    public POSSaleResponse hold(@PathVariable UUID saleId) {
        return saleService.hold(saleId);
    }

    @PatchMapping("/{saleId}/resume")
    @PreAuthorize(PosPermissions.Has.SALE_CREATE)
    public POSSaleResponse resume(@PathVariable UUID saleId) {
        return saleService.resume(saleId);
    }

    @PatchMapping("/{saleId}/cancel")
    @PreAuthorize(PosPermissions.Has.SALE_CANCEL)
    public POSSaleResponse cancel(@PathVariable UUID saleId) {
        return saleService.cancel(saleId);
    }

    @PostMapping("/{saleId}/complete")
    @PreAuthorize(PosPermissions.Has.SALE_CREATE)
    public POSSaleResponse complete(@PathVariable UUID saleId, @Valid @RequestBody CompleteSaleRequest request) {
        return saleService.complete(saleId, request);
    }

    @PostMapping("/{saleId}/complete-on-credit")
    @PreAuthorize(PosPermissions.Has.COMPLETE_ON_CREDIT)
    public POSCreditSaleResponse completeOnCredit(@PathVariable UUID saleId,
                                                  @Valid @RequestBody CompleteSaleOnCreditRequest request) {
        return saleService.completeOnCredit(saleId, request);
    }

    @GetMapping("/{saleId}")
    @PreAuthorize(PosPermissions.Has.SALE_VIEW)
    public POSSaleResponse findById(@PathVariable UUID saleId) {
        return saleService.findById(saleId);
    }
}
