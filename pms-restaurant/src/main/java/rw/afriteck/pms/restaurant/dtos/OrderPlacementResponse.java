package rw.afriteck.pms.restaurant.dtos;

import java.util.List;

public record OrderPlacementResponse(
        String billNo,
        String tableNumber,
        List<KitchenTicketGroupResponse> ticketGroups) {
}
