package rw.afriteck.pms.restaurant.dtos;

import rw.afriteck.pms.restaurant.enums.EMenuItemType;

import java.util.List;

public record KitchenTicketGroupResponse(
        EMenuItemType type,     // FOOD or BEVERAGE
        List<TableBillItemResponse> items
) {
}
