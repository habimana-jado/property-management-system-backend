package rw.afriteck.pms.stock.api.query;

import java.util.UUID;

public record StockOnHandFilter(UUID branchId, UUID storeId, UUID categoryId,
                                String search, boolean includeZero) {}
