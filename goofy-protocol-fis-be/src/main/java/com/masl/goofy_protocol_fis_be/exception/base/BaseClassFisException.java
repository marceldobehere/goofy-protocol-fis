package com.masl.goofy_protocol_fis_be.exception.base;

import java.util.Map;

public class BaseClassFisException extends RuntimeException {
    public int httpCode;
    public int errorCode;
    public String message;
    public Map<String, Object> errorDetails;

    public BaseClassFisException(int httpCode, int errorCode, String message, Map<String, Object> errorDetails) {
        super(message);
        this.httpCode = httpCode;
        this.errorCode = errorCode;
        this.message = message;
        this.errorDetails = errorDetails;
    }

    public BaseClassFisException(int httpCode, int errorCode, String message) {
        super(message);
        this.httpCode = httpCode;
        this.errorCode = errorCode;
        this.message = message;
        this.errorDetails = Map.of();
    }
}
