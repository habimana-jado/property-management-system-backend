package rw.afriteck.pms.pos.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record POSSaleItemResponse(
        UUID id,
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal unitPriceAtSaleTime,
        BigDecimal discountAmount,
        BigDecimal lineTotal
) {
}
