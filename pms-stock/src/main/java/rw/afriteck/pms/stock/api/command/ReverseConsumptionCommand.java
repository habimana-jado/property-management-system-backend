package rw.afriteck.pms.stock.api.command;

import rw.afriteck.pms.stock.api.enums.EMovementSourceType;

import java.util.UUID;

public record ReverseConsumptionCommand(
        EMovementSourceType sourceType,
        UUID sourceId,
        UUID sourceLineId,
        String reason,
        UUID performedBy
) {
}
