package rw.afriteck.pms.stock.internal.dtos;

import java.util.UUID;

public record BalanceKey(
        UUID storeId,
        UUID itemId
) {
}
