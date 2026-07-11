package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.IDENTITY_ENTRY_ALREADY_EXISTS, detailFields = {"handle"})
public class IdentityEntryAlreadyExists extends BaseClientFisException {
    public IdentityEntryAlreadyExists(String handle) {
        super("Identity Entry for \"" + handle + "\" already exists", Map.of("handle", handle));
    }
}
