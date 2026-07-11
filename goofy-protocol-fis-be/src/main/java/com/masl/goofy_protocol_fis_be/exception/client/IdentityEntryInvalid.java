package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.IDENTITY_ENTRY_INVALID)
public class IdentityEntryInvalid extends BaseClientFisException {
    public IdentityEntryInvalid() {
        super("Identity Entry is not Valid");
    }
}
