package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.LOGIN_ENTRY_ALREADY_EXISTS, detailFields = {"usernameHash"})
public class LoginEntryAlreadyExists extends BaseClientFisException {
    public LoginEntryAlreadyExists(String usernameHash) {
        super("Login Entry for \"" + usernameHash + "\" already exists", Map.of("usernameHash", usernameHash));
    }
}
