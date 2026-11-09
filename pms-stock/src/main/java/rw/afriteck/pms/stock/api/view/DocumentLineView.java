package rw.afriteck.pms.stock.api.view;

import java.math.BigDecimal;
import java.util.UUID;

public record DocumentLineView(UUID itemId, String itemCode, String itemName, String baseUnitCode,
                               BigDecimal quantity, BigDecimal enteredQuantity, String enteredUnitCode) {}
