package rw.afriteck.pms.stock.api.command;

import java.math.BigDecimal;
import java.util.UUID;

public record DocumentLineInput(
        UUID itemId,
        BigDecimal quantity,
        UUID unitId
) {
}
