package rw.afriteck.pms.stock.api;

import java.math.BigDecimal;
import java.util.UUID;

public interface StocktakeApi {
    UUID start(UUID storeId, UUID startedBy);
    void recordCount(UUID stocktakeId, UUID itemId, BigDecimal countedQuantity, UUID unitId);
    void submit(UUID stocktakeId);
    UUID approve(UUID stocktakeId, UUID approvedBy);   // adjustment document id, or null if no variance
    void cancel(UUID stocktakeId);
}
