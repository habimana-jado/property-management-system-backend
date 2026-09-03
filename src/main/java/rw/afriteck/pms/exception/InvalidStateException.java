package rw.afriteck.pms.exception;

public class InvalidStateException extends BusinessException {
    public InvalidStateException(String message) {
        super("INVALID_STATE", message);
    }
}
