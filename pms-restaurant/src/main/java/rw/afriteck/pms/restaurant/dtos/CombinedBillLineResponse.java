package rw.afriteck.pms.restaurant.dtos;

import java.math.BigDecimal;

public record CombinedBillLineResponse(
        String menuItemName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
