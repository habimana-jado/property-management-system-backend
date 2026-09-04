package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.ETableStatus;

import java.util.UUID;

public record TableMasterResponse(
        UUID tableMasterId,
        String tableNumber,
        int tableCapacity,
        ETableStatus tableStatus,
        UUID restaurantId
) {
}
