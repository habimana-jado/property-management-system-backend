package rw.afriteck.pms.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record TableBillItemResponse(
        UUID id,
        Integer transactionQuantity,
        String remarks,
        BigDecimal unitPriceAtOrderTime,
        BigDecimal lineTotal,

        MenuMasterSummary menuMaster

) {
}
