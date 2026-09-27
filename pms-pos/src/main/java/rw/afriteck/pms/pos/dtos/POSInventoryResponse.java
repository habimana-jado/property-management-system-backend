package rw.afriteck.pms.pos.dtos;

import java.util.UUID;

public record POSInventoryResponse(
        UUID productId,
        UUID hotelBranchId,
        Integer quantityOnHand
) {
}
