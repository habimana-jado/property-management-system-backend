package rw.afriteck.pms.common.exception;

public class AuthenticationRequiredException  extends RuntimeException {

    public AuthenticationRequiredException(String message) {
        super(message);
    }
}