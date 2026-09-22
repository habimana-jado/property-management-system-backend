package rw.afriteck.pms.dtos;

import java.util.List;

public record OrderPlacementResponse(
        String billNo,
        String tableNumber,
        List<KitchenTicketGroupResponse> ticketGroups) {
}
