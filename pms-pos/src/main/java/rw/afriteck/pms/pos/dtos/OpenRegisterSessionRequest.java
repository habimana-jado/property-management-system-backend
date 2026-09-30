package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OpenRegisterSessionRequest(
        @NotNull UUID terminalId,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal openingCash
) {
}
