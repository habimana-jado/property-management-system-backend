package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StockAdjustmentRequest(
        @NotNull UUID productId,
        @NotNull UUID hotelBranchId,
        @NotNull Integer quantityDelta, // positive = found more, negative = found less
        @NotNull EAdjustmentReason reason,
        String note
) {
}
