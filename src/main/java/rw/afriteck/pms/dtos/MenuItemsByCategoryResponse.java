package rw.afriteck.pms.dtos;

import java.util.List;

public record MenuItemsByCategoryResponse(
        String categoryName,
        List<MenuItemSearchResponse> items
) {
}
