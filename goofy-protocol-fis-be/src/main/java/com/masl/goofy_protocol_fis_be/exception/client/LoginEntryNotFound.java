package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.LOGIN_ENTRY_NOT_FOUND, detailFields = {"usernameHash"})
public class LoginEntryNotFound extends BaseClientFisException {
    public LoginEntryNotFound(String usernameHash) {
        super("Login Entry for \"" + usernameHash + "\" not found", Map.of("usernameHash", usernameHash));
    }
}
