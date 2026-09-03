package rw.afriteck.pms.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j // Lombok, gives you a `log` field
public class GlobalExceptionHandler {

    // ---- Domain/business exceptions ----

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.info("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(
            InvalidStateException ex, HttpServletRequest request) {
        log.info("Invalid state transition: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage(), request);
    }

    // ---- Bean Validation (@Valid on @RequestBody) ----

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse body = new ErrorResponse(
                "VALIDATION_FAILED",
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                Instant.now(),
                request.getRequestURI(),
                fieldErrors);

        log.info("Validation failed on {}: {}", request.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // ---- Validation on @PathVariable / @RequestParam (needs @Validated on the controller class) ----

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> new ErrorResponse.FieldError(
                        cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();

        ErrorResponse body = new ErrorResponse(
                "CONSTRAINT_VIOLATION", "Request parameters invalid",
                HttpStatus.BAD_REQUEST.value(), Instant.now(),
                request.getRequestURI(), fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    // ---- Malformed JSON / unreadable body ----

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.info("Malformed request body on {}", request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Request body is missing or malformed", request);
    }

    // ---- Wrong type in path variable, e.g. /orders/abc instead of /orders/1 ----

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Parameter '%s' has invalid value '%s'"
                .formatted(ex.getName(), ex.getValue());
        return build(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message, request);
    }

    // ---- 404 for unmapped routes (requires the property below) ----

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(
            NoHandlerFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND",
                "No handler for %s %s".formatted(ex.getHttpMethod(), ex.getRequestURL()), request);
    }

    // ---- Data integrity (e.g. DB unique constraint violated) ----

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "The request could not be completed due to a data conflict", request);
    }

    // ---- Access denied (Spring Security) ----

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "You do not have permission to perform this action", request);
    }

    // ---- Fallback — anything unanticipated ----

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        // This is the only place you log at ERROR with full stack trace —
        // everything above is an expected/handled case.
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String errorCode, String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                errorCode, message, status.value(), request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
