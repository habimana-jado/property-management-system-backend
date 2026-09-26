package rw.afriteck.pms.common.exception;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message);
    }
}
