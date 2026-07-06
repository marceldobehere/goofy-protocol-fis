package com.masl.goofy_protocol_fis_be.exception.base;

import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(httpStatus = 500)
public class BaseServerFisException extends BaseClassFisException {
    public BaseServerFisException(String message, Map<String, Object> errorDetails) {
        super(message, errorDetails);
    }

    public BaseServerFisException(String message) {
        super(message);
    }
}
