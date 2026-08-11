package com.masl.goofy_protocol_fis_be.exception;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClassFisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage());
    }

    // Handling all FIS Exceptions
    @ExceptionHandler(BaseClassFisException.class)
    public ResponseEntity<String> handleBaseClassFixException(BaseClassFisException ex) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String res = objectMapper.writeValueAsString(Map.of(
                "errorCode", ex.errorCode,
                "message", ex.message,
                "details", ex.errorDetails));

        return ResponseEntity
                .status(ex.httpCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(res);
    }

    // Handling HttpRequestMethodNotSupported
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage());
    }

    // Handling MissingServletRequestParameterException
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage());
    }

    // Handling NoResourceFound Exceptions
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage());
    }

    // Handling IllegalArgumentException Exceptions
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleRuntimeException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Internal server error :(");
    }

    // Handling CannotCreateTransactionException Exceptions
    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<String> handleRuntimeException(CannotCreateTransactionException ex) {
        log.error("CannotCreateTransactionException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Internal JPA Exception, most likely related to overloaded locks.");
    }

    // Handling Runtime Exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage());
    }

    // Handling Base Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("General Exception: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("An unexpected Error occurred!");
    }

    // Handle DataIntegrityViolation
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Data integrity violation occurred: " + ex.getMessage());
    }

    // Validation Exceptions

    public record ValidationError(String field, String message) {}
    public record ValidationErrorResponse(String message, List<ValidationError> invalidFields) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationError> invalidFields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse("Invalid Input", invalidFields));
    }

    private ValidationError toValidationError(FieldError error) {
        return new ValidationError(
                error.getField(),
                error.getDefaultMessage() != null ? error.getDefaultMessage() : "invalid"
        );
    }
}
