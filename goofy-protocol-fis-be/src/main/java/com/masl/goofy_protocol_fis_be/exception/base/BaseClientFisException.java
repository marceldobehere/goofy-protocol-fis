package com.masl.goofy_protocol_fis_be.exception.base;

import java.util.Map;

public class BaseClientFisException extends BaseClassFisException {
    public BaseClientFisException(int errorCode, String message, Map<String, Object> errorDetails) {
        super(400, errorCode, message, errorDetails);
    }

    public BaseClientFisException(int errorCode, String message) {
        super(400, errorCode, message);
    }
}
