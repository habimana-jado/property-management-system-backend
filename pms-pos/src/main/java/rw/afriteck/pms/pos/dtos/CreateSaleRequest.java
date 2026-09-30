package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSaleRequest(
        @NotNull UUID terminalId,
        UUID customerId
) {
}
