package com.masl.goofy_protocol_fis_be.exception.base;

import java.util.Map;

public class BaseServerFisException extends BaseClassFisException {
    public BaseServerFisException(int errorCode, String message, Map<String, Object> errorDetails) {
        super(400, errorCode, message, errorDetails);
    }

    public BaseServerFisException(int errorCode, String message) {
        super(400, errorCode, message);
    }
}
