package rw.afriteck.pms.dtos;

import java.math.BigDecimal;

public record CombinedBillLineResponse(
        String menuItemName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
