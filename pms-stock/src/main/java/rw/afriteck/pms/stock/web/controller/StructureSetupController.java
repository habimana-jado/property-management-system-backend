package rw.afriteck.pms.stock.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.StructureSetupApi;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.web.dtos.ActiveRequest;
import rw.afriteck.pms.stock.web.dtos.BranchSettingsRequest;
import rw.afriteck.pms.stock.web.dtos.RenameRequest;
import rw.afriteck.pms.stock.web.security.StockPermissions;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/stock/setup")
@RequiredArgsConstructor
public class StructureSetupController {

    private final StructureSetupApi structure;

    // ---- stores ----
    @GetMapping("/stores")
    @PreAuthorize(StockPermissions.Has.VIEW)
    public List<StoreView> stores(@RequestParam UUID branchId,
                                  @RequestParam(defaultValue = "false") boolean includeInactive) {
        return structure.stores(branchId, includeInactive);
    }

    @PostMapping("/stores")
    @PreAuthorize(StockPermissions.Has.STORE_MANAGE)
    public ResponseEntity<StoreView> createStore(@Valid @RequestBody StoreCommand c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(structure.createStore(c));
    }

    @PatchMapping("/stores/{id}")
    @PreAuthorize(StockPermissions.Has.STORE_MANAGE)
    public StoreView renameStore(@PathVariable UUID id, @Valid @RequestBody RenameRequest r) {
        return structure.renameStore(id, r.name());
    }

    @PatchMapping("/stores/{id}/active")
    @PreAuthorize(StockPermissions.Has.STORE_MANAGE)
    public ResponseEntity<Void> setStoreActive(@PathVariable UUID id, @RequestBody ActiveRequest r) {
        structure.setStoreActive(id, r.active());
        return ResponseEntity.noContent().build();
    }

    // ---- assignments ----
    @GetMapping("/assignments")
    @PreAuthorize(StockPermissions.Has.VIEW)
    public List<AssignmentView> assignments(@RequestParam(required = false) UUID storeId,
                                            @RequestParam(required = false) EOutletType outletType,
                                            @RequestParam(required = false) UUID outletId) {
        if (storeId != null) return structure.assignmentsForStore(storeId);
        if (outletType != null && outletId != null) return structure.assignmentsForOutlet(outletType, outletId);
        throw new StockRuleViolationException("Provide storeId, or outletType and outletId");
    }

    @PostMapping("/assignments")
    @PreAuthorize(StockPermissions.Has.STORE_MANAGE)
    public ResponseEntity<AssignmentView> assign(@Valid @RequestBody AssignmentCommand c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(structure.assign(c));
    }

    @PostMapping("/assignments/{id}/default")
    @PreAuthorize(StockPermissions.Has.STORE_MANAGE)
    public ResponseEntity<Void> makeDefault(@PathVariable UUID id) {
        structure.makeDefault(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize(StockPermissions.Has.STORE_MANAGE)
    public ResponseEntity<Void> unassign(@PathVariable UUID id) {
        structure.unassign(id);
        return ResponseEntity.noContent().build();
    }

    // ---- branch settings ----
    @GetMapping("/branches/{branchId}/settings")
    @PreAuthorize(StockPermissions.Has.VIEW)
    public BranchSettingsView settings(@PathVariable UUID branchId) { return structure.branchSettings(branchId); }

    @PutMapping("/branches/{branchId}/settings")
    @PreAuthorize(StockPermissions.Has.SETTINGS_MANAGE)
    public BranchSettingsView updateSettings(@PathVariable UUID branchId, @RequestBody BranchSettingsRequest r) {
        return structure.updateBranchSettings(branchId, r.allowNegativeStock());
    }
}