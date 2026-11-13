package rw.afriteck.pms.common.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientStockException extends BusinessRuleViolationException {

    private final BigDecimal available;
    private final BigDecimal required;

    public InsufficientStockException(String storeName, String itemName,
                                      BigDecimal available, BigDecimal required) {
        super("INSUFFICIENT_STOCK",
                "Insufficient stock of " + itemName + " in " + storeName
                        + ": available " + available + ", required " + required);
        this.available = available;
        this.required = required;
    }

}
