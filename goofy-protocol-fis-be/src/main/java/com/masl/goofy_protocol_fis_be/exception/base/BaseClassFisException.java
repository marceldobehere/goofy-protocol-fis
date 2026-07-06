package com.masl.goofy_protocol_fis_be.exception.base;

import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

public class BaseClassFisException extends Exception {
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
        this(httpCode, errorCode, message, Map.of());
    }

    public BaseClassFisException(String message, Map<String, Object> errorDetails) {
        super(message);
        this.httpCode = httpStatusFor(getClass());
        this.errorCode = errorCodeFor(getClass());
        this.message = message;
        this.errorDetails = errorDetails;
    }

    public BaseClassFisException(String message) {
        this(message, Map.of());
    }

    // Magic Helper Methods for Annotation
    public static int httpStatusFor(Class<?> _class) {
        // Check Base Class
        FisHttpErrorCode ann = _class.getAnnotation(FisHttpErrorCode.class);
        if (ann == null)
            throw new IllegalArgumentException("Class " + _class.getName() + " is not annotated with @FisHttpErrorCode");
        if (ann.httpStatus() != 0)
            return ann.httpStatus();

        // Check Super Class
        ann = _class.getSuperclass().getAnnotation(FisHttpErrorCode.class);
        if (ann == null)
            throw new IllegalArgumentException("Class " + _class.getName() + " is not annotated with @FisHttpErrorCode");
        if (ann.httpStatus() != 0)
            return ann.httpStatus();

        throw new IllegalArgumentException("Class " + _class.getName() + " is not annotated with @FisHttpErrorCode");
    }

    // Magic Helper Methods for Annotation
    public static int errorCodeFor(Class<?> _class) {
        // Check Base Class
        FisHttpErrorCode ann = _class.getAnnotation(FisHttpErrorCode.class);
        if (ann == null)
            throw new IllegalArgumentException("Class " + _class.getName() + " is not annotated with @FisHttpErrorCode");
        if (ann.errorCode() != 0)
            return ann.errorCode();

        // Check Super Class
        ann = _class.getSuperclass().getAnnotation(FisHttpErrorCode.class);
        if (ann == null)
            throw new IllegalArgumentException("Class " + _class.getName() + " is not annotated with @FisHttpErrorCode");
        if (ann.errorCode() != 0)
            return ann.errorCode();

        throw new IllegalArgumentException("Class " + _class.getName() + " is not annotated with @FisHttpErrorCode");
    }
}
