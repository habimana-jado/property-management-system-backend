package rw.afriteck.pms.restaurant.dtos;

import rw.afriteck.pms.restaurant.enums.EBillStatus;

import java.math.BigDecimal;
import java.util.List;

public record TableBillResponse(

        //Bill Details
        String billNo,
        EBillStatus billStatus,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,

        //Table Details
        String tableNumber,

        //Table Bill Item Details
        List<TableBillItemSummary> tableBillItems

) {
    public static TableBillResponse empty(String tableNumber) {
        return new TableBillResponse(
                null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                tableNumber,
                List.of()
        );
    }
}
