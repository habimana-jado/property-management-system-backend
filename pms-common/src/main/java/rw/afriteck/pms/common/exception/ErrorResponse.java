package rw.afriteck.pms.common.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String errorCode,
        String message,
        int status,
        Instant timestamp,
        String path,
        List<FieldError> fieldErrors  // null/empty when not a validation error
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(String errorCode, String message, int status, String path) {
        return new ErrorResponse(errorCode, message, status, Instant.now(), path, null);
    }
}