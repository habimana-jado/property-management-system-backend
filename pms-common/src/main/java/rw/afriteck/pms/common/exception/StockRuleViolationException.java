package rw.afriteck.pms.common.exception;

public class StockRuleViolationException extends BusinessRuleViolationException {
    public StockRuleViolationException(String message) {
        super("STOCK_RULE_VIOLATION", message);
    }
}