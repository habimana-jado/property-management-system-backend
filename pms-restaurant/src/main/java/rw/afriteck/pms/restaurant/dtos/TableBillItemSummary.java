package rw.afriteck.pms.restaurant.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record TableBillItemSummary(
        UUID id,
        String menuItemName,
        Integer transactionQuantity,
        BigDecimal unitPriceAtOrderTime,
        BigDecimal lineTotal
) {
}
