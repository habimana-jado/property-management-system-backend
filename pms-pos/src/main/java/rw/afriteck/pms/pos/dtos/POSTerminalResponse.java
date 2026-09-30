package rw.afriteck.pms.pos.dtos;

import rw.afriteck.pms.common.enums.ERecordStatus;

import java.util.UUID;

public record POSTerminalResponse(
        UUID id,
        UUID hotelBranchId,
        String code,
        String name,
        ERecordStatus status
) {
}
