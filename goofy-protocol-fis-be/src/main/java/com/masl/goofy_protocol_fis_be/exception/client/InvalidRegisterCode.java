package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;

import java.util.Map;

public class InvalidRegisterCode extends BaseClientFisException {
    public InvalidRegisterCode(String code) {
        super(AllClientErrorCodes.INVALID_REGISTER_CODE, "Invalid register code: " + code, Map.of("code", code));
    }
}
