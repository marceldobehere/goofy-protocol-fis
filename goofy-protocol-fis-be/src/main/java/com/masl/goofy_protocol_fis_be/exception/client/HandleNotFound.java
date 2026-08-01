package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(httpStatus = 404, errorCode = AllClientErrorCodes.HANDLE_NOT_FOUND, detailFields = {"handle"})
public class HandleNotFound extends BaseClientFisException {
    public HandleNotFound(String handle) {
        super("User with Handle \"" + handle + "\" not found", Map.of("handle", handle));
    }
}
