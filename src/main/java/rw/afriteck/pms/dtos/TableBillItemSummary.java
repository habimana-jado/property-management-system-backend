package rw.afriteck.pms.dtos;

import java.math.BigDecimal;

public record TableBillItemSummary(
        //Order Details
        String menuItemName,
        Integer transactionQuantity,
        BigDecimal unitPriceAtOrderTime,
        BigDecimal lineTotal
) {
}
