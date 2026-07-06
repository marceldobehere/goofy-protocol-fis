package com.masl.goofy_protocol_fis_be.exception.base;

import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(httpStatus = 400)
public class BaseClientFisException extends BaseClassFisException {
    public BaseClientFisException(String message, Map<String, Object> errorDetails) {
        super(message, errorDetails);
    }

    public BaseClientFisException(String message) {
        super(message);
    }
}
