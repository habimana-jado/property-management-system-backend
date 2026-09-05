package rw.afriteck.pms.dtos;

import java.util.UUID;

public record MenuCategoryResponse(
        UUID id,
        String name,
        UUID restaurantId
) {
}
