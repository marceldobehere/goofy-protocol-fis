package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.REGISTRATION_NOT_ALLOWED)
public class RegistrationNotAllowed extends BaseClientFisException {
    public RegistrationNotAllowed() {
        super("Registrations are not allowed");
    }
}
