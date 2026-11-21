package rw.afriteck.pms.stock.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.stock.api.CatalogSetupApi;
import rw.afriteck.pms.stock.api.enums.EItemType;
import rw.afriteck.pms.stock.api.query.ItemSearch;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.api.view.PageResult;
import rw.afriteck.pms.stock.web.dtos.ActiveRequest;
import rw.afriteck.pms.stock.web.dtos.RenameRequest;
import rw.afriteck.pms.stock.web.security.StockPermissions;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/stock/setup")
@RequiredArgsConstructor
public class CatalogSetupController {

    private final CatalogSetupApi catalog;

    // ---- units ----
    @GetMapping("/units")
    @PreAuthorize(StockPermissions.Has.CATALOG_VIEW)
    public List<UnitView> units() { return catalog.units(); }

    @PostMapping("/units")
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_HOTELS')")
    public ResponseEntity<UnitView> createUnit(@Valid @RequestBody UnitCommand c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalog.createUnit(c));
    }

    @PatchMapping("/units/{id}")
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_HOTELS')")
    public UnitView renameUnit(@PathVariable UUID id, @Valid @RequestBody RenameRequest r) {
        return catalog.renameUnit(id, r.name());
    }

    // ---- categories ----
    @GetMapping("/categories")
    @PreAuthorize(StockPermissions.Has.CATALOG_VIEW)
    public List<CategoryView> categories(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return catalog.categories(includeInactive);
    }

    @PostMapping("/categories")
    @PreAuthorize(StockPermissions.Has.CATALOG_MANAGE)
    public ResponseEntity<CategoryView> createCategory(@Valid @RequestBody CategoryCommand c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalog.createCategory(c));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize(StockPermissions.Has.CATALOG_MANAGE)
    public CategoryView updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryUpdateCommand c) {
        return catalog.updateCategory(id, c);
    }

    @PatchMapping("/categories/{id}/active")
    @PreAuthorize(StockPermissions.Has.CATALOG_MANAGE)
    public ResponseEntity<Void> setCategoryActive(@PathVariable UUID id, @RequestBody ActiveRequest r) {
        catalog.setCategoryActive(id, r.active());
        return ResponseEntity.noContent().build();
    }

    // ---- items ----
    @GetMapping("/items")
    @PreAuthorize(StockPermissions.Has.CATALOG_VIEW)
    public PageResult<ItemView> searchItems(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) EItemType type,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return catalog.searchItems(new ItemSearch(text, categoryId, type, activeOnly), page, size);
    }

    @GetMapping("/items/{id}")
    @PreAuthorize(StockPermissions.Has.CATALOG_VIEW)
    public ItemView item(@PathVariable UUID id) { return catalog.item(id); }

    @PostMapping("/items")
    @PreAuthorize(StockPermissions.Has.CATALOG_MANAGE)
    public ResponseEntity<ItemView> createItem(@Valid @RequestBody ItemCommand c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalog.createItem(c));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize(StockPermissions.Has.CATALOG_MANAGE)
    public ItemView updateItem(@PathVariable UUID id, @Valid @RequestBody ItemUpdateCommand c) {
        return catalog.updateItem(id, c);
    }

    @PatchMapping("/items/{id}/active")
    @PreAuthorize(StockPermissions.Has.CATALOG_MANAGE)
    public ResponseEntity<Void> setItemActive(@PathVariable UUID id, @RequestBody ActiveRequest r) {
        catalog.setItemActive(id, r.active());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{id}/packagings")
    @PreAuthorize(StockPermissions.Has.CATALOG_MANAGE)
    public ItemView putPackaging(@PathVariable UUID id, @Valid @RequestBody PackagingCommand c) {
        return catalog.putPackaging(id, c);
    }

    @DeleteMapping("/items/{id}/packagings/{unitId}")
    @PreAuthorize(StockPermissions.Has.CATALOG_MANAGE)
    public ItemView removePackaging(@PathVariable UUID id, @PathVariable UUID unitId) {
        return catalog.removePackaging(id, unitId);
    }
}