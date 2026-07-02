package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;

public class RegistrationNotAllowed extends BaseClientFisException {
    public RegistrationNotAllowed() {
        super(AllClientErrorCodes.REGISTRATION_NOT_ALLOWED, "Registrations are not allowed");
    }
}
