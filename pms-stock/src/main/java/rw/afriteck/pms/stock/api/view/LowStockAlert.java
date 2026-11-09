package rw.afriteck.pms.stock.api.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LowStockAlert(UUID itemId, String itemCode, String itemName, String baseUnitCode,
                            BigDecimal reorderLevel, BigDecimal totalOnHand, List<StoreQuantity> stores) {}
