package rw.afriteck.pms.stock.web.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;

import java.math.BigDecimal;
import java.util.UUID;

public record DocumentLineRequest(
        @NotNull UUID itemId,
        @NotNull @Positive BigDecimal quantity,
        @NotNull UUID unitId
) implements LineRequest {
    @Override public DocumentLineInput toInput() { return new DocumentLineInput(itemId, quantity, unitId); }
}
