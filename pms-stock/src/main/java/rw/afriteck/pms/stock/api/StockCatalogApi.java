package rw.afriteck.pms.stock.api;

import java.util.UUID;

/** For pms-pos and pms-restaurant: one place for the "stock item or recipe, never both" rules. */
public interface StockCatalogApi {
    /** Throws StockRuleViolationException when the mapping breaks a rule. Call it when saving a product or menu item. */
    void validateSaleMapping(boolean trackInventory, UUID stockItemId, UUID recipeId, boolean recipeAllowed);
}
