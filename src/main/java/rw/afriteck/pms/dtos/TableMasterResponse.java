package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.enums.ETableStatus;

import java.util.UUID;

public record TableMasterResponse(
        UUID id,
        String tableNumber,
        int tableCapacity,
        ETableStatus tableStatus,
        ERecordStatus recordStatus,
        UUID restaurantId
) {
}
