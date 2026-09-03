package rw.afriteck.pms.exception;

public class BusinessRuleViolationException extends BusinessException {
    public BusinessRuleViolationException(String errorCode, String message) {
        super(errorCode, message);
    }
}
