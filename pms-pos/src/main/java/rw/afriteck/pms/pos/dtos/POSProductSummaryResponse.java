package rw.afriteck.pms.pos.dtos;

import java.util.UUID;

public record POSProductSummaryResponse(
        UUID id,
        String name,
        String imageUrl
) {
}
