package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.StockCatalogApi;
import rw.afriteck.pms.stock.internal.model.Recipe;
import rw.afriteck.pms.stock.internal.model.StockItem;
import rw.afriteck.pms.stock.internal.repository.RecipeRepository;
import rw.afriteck.pms.stock.internal.repository.StockItemRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StockCatalogServiceImpl implements StockCatalogApi {

    private final StockItemRepository items;
    private final RecipeRepository recipes;

    @Override
    public void validateSaleMapping(boolean trackInventory, UUID stockItemId, UUID recipeId, boolean recipeAllowed) {
        require(stockItemId == null || recipeId == null, "A product maps to a stock item or a recipe, never both");
        require(recipeAllowed || recipeId == null, "This kind of product cannot use a recipe");
        if (trackInventory) {
            require(stockItemId != null || recipeId != null,
                    "A tracked product needs a stock item" + (recipeAllowed ? " or a recipe" : ""));
        }
        if (stockItemId != null) {
            require(items.findById(stockItemId).filter(StockItem::isActive).isPresent(),
                    "Unknown or inactive stock item");
        }
        if (recipeId != null) {
            require(recipes.findById(recipeId).filter(Recipe::isActive).isPresent(),
                    "Unknown or inactive recipe");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new StockRuleViolationException(message);
    }
}
