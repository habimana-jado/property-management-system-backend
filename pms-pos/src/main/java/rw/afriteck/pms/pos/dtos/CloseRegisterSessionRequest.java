package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CloseRegisterSessionRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal actualClosingCash
) {
}
