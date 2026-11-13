package rw.afriteck.pms.common.exception;

public class StockNotFoundException extends ResourceNotFoundException {
    public StockNotFoundException(String message) {
        super("STOCK_NOT_FOUND", message);
    }
}