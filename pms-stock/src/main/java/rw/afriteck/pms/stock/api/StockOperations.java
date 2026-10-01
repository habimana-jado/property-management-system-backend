package rw.afriteck.pms.stock.api;

import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.command.TransferCommand;

import java.util.UUID;

public interface StockOperations {
    /** RECEIPT, OPENING_BALANCE, ADJUSTMENT, WRITE_OFF, SUPPLIER_RETURN, CUSTOMER_RETURN, CONSUMPTION. Returns document id. */
    UUID postDocument(SingleStoreDocumentCommand command);

    /** DISTRIBUTION, RETURN, DAMAGE_REPORT. Returns document id. */
    UUID transfer(TransferCommand command);
}
