package rw.afriteck.pms.pos.dtos;

import rw.afriteck.pms.pos.enums.ESaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record POSSaleResponse(
        UUID id,
        String saleNumber,
        UUID cashierId,
        UUID customerId,
        ESaleStatus status,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal taxTotal,
        BigDecimal grandTotal,
        List<POSSaleItemResponse> items,
        LocalDateTime completedAt
) {
}
