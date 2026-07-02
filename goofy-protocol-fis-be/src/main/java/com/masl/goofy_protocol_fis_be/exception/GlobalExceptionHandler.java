package com.masl.goofy_protocol_fis_be.exception;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClassFisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage());
    }

    // Handling the Base Class
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

    // Handling Base Exceptions
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("General Exception: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("An unexpected Error occurred!");
    }
}
