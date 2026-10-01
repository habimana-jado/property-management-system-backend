package rw.afriteck.pms.common.exception;

import java.math.BigDecimal;

public class InsufficientStockException extends RuntimeException {

    private final BigDecimal available;
    private final BigDecimal required;
    public InsufficientStockException(String storeName, String itemName,
                                      BigDecimal available, BigDecimal required) {
        super("Insufficient stock of " + itemName + " in " + storeName
                + ": available " + available + ", required " + required);
        this.available = available;
        this.required = required;
    }
    public BigDecimal getAvailable() { return available; }
    public BigDecimal getRequired() { return required; }
}
