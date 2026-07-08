package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.LOGIN_ENTRY_INVALID)
public class LoginEntryInvalid extends BaseClientFisException {
    public LoginEntryInvalid() {
        super("Login Entry is not Valid");
    }
}
