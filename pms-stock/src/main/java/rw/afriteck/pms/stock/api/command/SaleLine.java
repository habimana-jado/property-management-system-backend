package rw.afriteck.pms.stock.api.command;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleLine(
        UUID sourceLineId,
        UUID stockItemId,
        UUID recipeId,
        BigDecimal quantity,
        boolean trackInventory) {
}
