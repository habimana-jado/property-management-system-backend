package rw.afriteck.pms.restaurant.dtos;

import java.util.List;

public record MenuItemsByCategoryResponse(
        String categoryName,
        List<MenuItemSearchResponse> items
) {
}
