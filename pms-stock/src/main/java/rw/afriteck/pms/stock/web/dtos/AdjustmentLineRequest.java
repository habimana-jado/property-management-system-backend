package rw.afriteck.pms.stock.web.dtos;

import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;

import java.math.BigDecimal;
import java.util.UUID;

public record AdjustmentLineRequest(
        @NotNull UUID itemId,
        @NotNull BigDecimal quantity,
        @NotNull UUID unitId) implements LineRequest {
    @Override public DocumentLineInput toInput() { return new DocumentLineInput(itemId, quantity, unitId); }
}

