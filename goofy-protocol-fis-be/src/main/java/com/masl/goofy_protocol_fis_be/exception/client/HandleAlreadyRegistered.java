package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.HANDLE_ALREADY_REGISTERED)
public class HandleAlreadyRegistered extends BaseClientFisException {
    public HandleAlreadyRegistered(String handle) {
        super("Handle already registered: " + handle, Map.of("handle", handle));
    }
}
