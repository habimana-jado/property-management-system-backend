package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.EMenuItemType;

import java.util.List;

public record KitchenTicketGroupResponse(
        EMenuItemType type,     // FOOD or BEVERAGE
        List<TableBillItemResponse> items
) {
}
