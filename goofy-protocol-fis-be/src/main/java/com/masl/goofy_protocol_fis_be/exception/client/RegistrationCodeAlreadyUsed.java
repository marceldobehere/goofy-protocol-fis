package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.REGISTRATION_CODE_ALREADY_USED, detailFields = {"code"})
public class RegistrationCodeAlreadyUsed extends BaseClientFisException {
    public RegistrationCodeAlreadyUsed(String code) {
        super("Code already used: " + code, Map.of("code", code));
    }
}
