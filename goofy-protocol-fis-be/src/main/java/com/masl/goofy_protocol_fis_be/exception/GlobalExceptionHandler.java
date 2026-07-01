package com.masl.goofy_protocol_fis_be.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    // TODO: Document correct Error Code and Message
    @ExceptionHandler(PublicKeyNotFoundException.class)
    public ResponseEntity<String> handlePublicKeyNotFoundException(PublicKeyNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Public Split Key could not be resolved");
    }

    // TODO: Document correct Error Code and Message
    @ExceptionHandler(InvalidSignatureException.class)
    public ResponseEntity<String> handleInvalidSignatureException(PublicKeyNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Signature for Signed Request is invalid");
    }
}
