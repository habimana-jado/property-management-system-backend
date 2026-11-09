package rw.afriteck.pms.stock.api;

import rw.afriteck.pms.stock.api.query.DocumentFilter;
import rw.afriteck.pms.stock.api.query.MovementFilter;
import rw.afriteck.pms.stock.api.query.StockOnHandFilter;
import rw.afriteck.pms.stock.api.query.StocktakeFilter;
import rw.afriteck.pms.stock.api.view.*;

import java.util.List;
import java.util.UUID;

public interface StockQueryApi {
    PageResult<StockOnHandRow> stockOnHand(StockOnHandFilter filter, int page, int size);
    PageResult<MovementRow> movements(MovementFilter filter, int page, int size);
    List<LowStockAlert> lowStock(UUID branchId);
    PageResult<StockDocumentSummary> documents(DocumentFilter filter, int page, int size);
    StockDocumentView document(UUID id);
    PageResult<StocktakeSummary> stocktakes(StocktakeFilter filter, int page, int size);
    StocktakeView stocktake(UUID id);
}
