package rw.afriteck.pms.common.exception;

public class BusinessRuleViolationException extends BusinessException {
    public BusinessRuleViolationException(String errorCode, String message) {
        super(errorCode, message);
    }
}
