package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.INVALID_REGISTER_CODE)
public class InvalidRegisterCode extends BaseClientFisException {
    public InvalidRegisterCode(String code) {
        super("Invalid register code: " + code, Map.of("code", code));
    }
}
