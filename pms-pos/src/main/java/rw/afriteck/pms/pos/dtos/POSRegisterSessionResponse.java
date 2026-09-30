package rw.afriteck.pms.pos.dtos;

import rw.afriteck.pms.pos.enums.ERegisterSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record POSRegisterSessionResponse(
        UUID id,
        UUID terminalId,
        UUID hotelBranchId,
        UUID cashierId,
        BigDecimal openingCash,
        BigDecimal expectedClosingCash,
        BigDecimal actualClosingCash,
        BigDecimal variance,
        ERegisterSessionStatus status,
        LocalDateTime openedAt,
        LocalDateTime closedAt
) {
}
