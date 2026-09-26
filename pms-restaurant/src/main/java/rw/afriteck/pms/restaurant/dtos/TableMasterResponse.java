package rw.afriteck.pms.restaurant.dtos;

import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.restaurant.enums.ETableStatus;

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
