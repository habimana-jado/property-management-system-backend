package rw.afriteck.pms.stock.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.stock.api.RecipeSetupApi;
import rw.afriteck.pms.stock.api.setup.RecipeCommand;
import rw.afriteck.pms.stock.api.setup.RecipeSummary;
import rw.afriteck.pms.stock.api.setup.RecipeView;
import rw.afriteck.pms.stock.api.view.PageResult;
import rw.afriteck.pms.stock.web.dtos.ActiveRequest;
import rw.afriteck.pms.stock.web.security.StockPermissions;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/stock/setup/recipes")
@RequiredArgsConstructor
public class RecipeSetupController {

    private final RecipeSetupApi recipes;

    @GetMapping
    @PreAuthorize(StockPermissions.Has.CATALOG_VIEW)
    public PageResult<RecipeSummary> list(@RequestParam(required = false) String text,
                                          @RequestParam(defaultValue = "true") boolean activeOnly,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "50") int size) {
        return recipes.recipes(text, activeOnly, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize(StockPermissions.Has.CATALOG_VIEW)
    public RecipeView get(@PathVariable UUID id) { return recipes.recipe(id); }

    @PostMapping
    @PreAuthorize(StockPermissions.Has.RECIPE_MANAGE)
    public ResponseEntity<RecipeView> create(@Valid @RequestBody RecipeCommand c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipes.createRecipe(c));
    }

    @PutMapping("/{id}")
    @PreAuthorize(StockPermissions.Has.RECIPE_MANAGE)
    public RecipeView update(@PathVariable UUID id, @Valid @RequestBody RecipeCommand c) {
        return recipes.updateRecipe(id, c);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize(StockPermissions.Has.RECIPE_MANAGE)
    public ResponseEntity<Void> setActive(@PathVariable UUID id, @RequestBody ActiveRequest r) {
        recipes.setRecipeActive(id, r.active());
        return ResponseEntity.noContent().build();
    }
}