package rw.afriteck.pms.restaurant.dtos;

import java.util.UUID;

public record MenuItemSearchResponse(
        UUID id,
        String name
) {
}
