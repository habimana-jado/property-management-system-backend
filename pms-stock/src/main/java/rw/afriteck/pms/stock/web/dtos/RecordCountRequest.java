package rw.afriteck.pms.stock.web.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordCountRequest(
        @NotNull UUID itemId,
        @NotNull @PositiveOrZero BigDecimal countedQuantity,
        @NotNull UUID unitId) {
}
