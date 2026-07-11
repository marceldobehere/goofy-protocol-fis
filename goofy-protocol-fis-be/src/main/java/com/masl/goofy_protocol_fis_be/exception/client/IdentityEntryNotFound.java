package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.IDENTITY_ENTRY_NOT_FOUND, detailFields = {"handle"})
public class IdentityEntryNotFound extends BaseClientFisException {
    public IdentityEntryNotFound(String handle) {
        super("Identity Entry for \"" + handle + "\" not found", Map.of("handle", handle));
    }
}
