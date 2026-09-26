package rw.afriteck.pms.restaurant.dtos;

import rw.afriteck.pms.restaurant.enums.EBillStatus;

import java.math.BigDecimal;
import java.util.List;

public record BillSnapshotResponse(

        //Bill Details
        String billNo,
        EBillStatus billStatus,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,

        //Table Details
        String tableNumber,

        //Table Bill Items Combined
        List<CombinedBillLineResponse> tableBillItemsCombined

) {
}
