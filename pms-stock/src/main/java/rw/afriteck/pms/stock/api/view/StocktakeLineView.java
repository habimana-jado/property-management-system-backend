package rw.afriteck.pms.stock.api.view;

import java.math.BigDecimal;
import java.util.UUID;

public record StocktakeLineView(UUID itemId, String itemCode, String itemName, String baseUnitCode,
                                BigDecimal expectedQuantity, BigDecimal countedQuantity, BigDecimal variance) {}
