package rw.afriteck.pms.stock.api;

import rw.afriteck.pms.stock.api.command.ConsumeForSaleCommand;
import rw.afriteck.pms.stock.api.command.ReverseConsumptionCommand;

import java.util.List;
import java.util.UUID;

public interface StockApi {

    /**
     * Deducts stock for a sale. Must be called inside the caller's transaction:
     * if it throws, the sale rolls back with it.
     * Lines with trackInventory=false are skipped entirely.
     * Returns the ids of the SALE movements created.
     */
    List<UUID> consumeForSale(ConsumeForSaleCommand command);

    /**
     * Restores stock by reversing every not-yet-reversed movement for the source
     * (a whole sale/bill, or one line if sourceLineId is given).
     * Returns the number of movements reversed (0 is fine: untracked lines).
     */
    int reverseConsumption(ReverseConsumptionCommand command);
}
