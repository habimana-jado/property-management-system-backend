package rw.afriteck.pms.dtos;

import java.util.UUID;

public record MenuItemSearchResponse(
        UUID id,
        String name
) {
}
