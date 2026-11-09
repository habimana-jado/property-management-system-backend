package rw.afriteck.pms.stock.api.view;

import java.math.BigDecimal;
import java.util.UUID;

public record StockOnHandRow(UUID storeId, String storeName, UUID itemId, String itemCode, String itemName,
                             UUID categoryId, String categoryName, String baseUnitCode, BigDecimal quantityOnHand) {}
