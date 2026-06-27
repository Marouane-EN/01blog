package _Blog_Backend.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private Map<String, Object> buildErrorResponse(HttpStatusCode status, String message) {
        return buildErrorResponseWithFields(status, message, null);
    }

    private Map<String, Object> buildErrorResponseWithFields(HttpStatusCode status, String message,
            Map<String, String> fieldErrors) {
        Map<String, Object> body = new LinkedHashMap<>();

        // Formats the timestamp cleanly (e.g., "2026-06-27T14:30:00")
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        body.put("status", status.value());

        // Automatically translates 400 to "Bad Request", 404 to "Not Found", etc.
        body.put("error", HttpStatus.valueOf(status.value()).getReasonPhrase());
        body.put("message", message);

        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            body.put("errors", fieldErrors);
        }
        return body;
    }

    // =========================================================================
    // SECTION 1: OVERRIDES (Spring's Built-in MVC Exceptions)
    // =========================================================================

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = buildErrorResponseWithFields(status, "Validation failed for one or more fields.",
                fieldErrors);
        return new ResponseEntity<>(body, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String message = String.format("The %s method is not supported for this endpoint. Supported methods are: %s.",
                ex.getMethod(), ex.getSupportedHttpMethods());

        return new ResponseEntity<>(buildErrorResponse(status, message), status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {

        String message = String.format("The required parameter '%s' of type '%s' is missing.",
                ex.getParameterName(), ex.getParameterType());

        return new ResponseEntity<>(buildErrorResponse(status, message), status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String message = String.format("The content type '%s' is not supported. Supported types are: %s.",
                ex.getContentType(), ex.getSupportedMediaTypes());

        return new ResponseEntity<>(buildErrorResponse(status, message), status);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return new ResponseEntity<>(buildErrorResponse(status, "File size exceeds the maximum limit of 100MB."),
                status);
    }

    // =========================================================================
    // SECTION 2: CUSTOM EXCEPTIONS
    // =========================================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        return new ResponseEntity<>(buildErrorResponse(ex.getStatusCode(), ex.getReason()), ex.getStatusCode());
    }

    @ExceptionHandler({ BadCredentialsException.class, InternalAuthenticationServiceException.class })
    public ResponseEntity<Map<String, Object>> handleAuthenticationExceptions(Exception ex) {
        return new ResponseEntity<>(
                buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username, email, or password."),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Valid Type";
        String message = String.format("The URL parameter '%s' must be of type '%s'. You provided: '%s'.",
                ex.getName(), expectedType, ex.getValue());

        return new ResponseEntity<>(buildErrorResponse(HttpStatus.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseConflicts(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(
                buildErrorResponse(HttpStatus.CONFLICT,
                        "Database conflict: The data provided violates a unique constraint or already exists."),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(buildErrorResponse(HttpStatus.FORBIDDEN,
                "You do not have the required permissions to access this resource."), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllOtherExceptions(Exception ex) {
        System.err.println("CRITICAL SERVER ERROR: " + ex.getMessage());
        ex.printStackTrace();
        return new ResponseEntity<>(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}