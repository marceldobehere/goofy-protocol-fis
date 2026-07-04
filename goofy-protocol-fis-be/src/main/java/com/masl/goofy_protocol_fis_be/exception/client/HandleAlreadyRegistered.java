package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;

import java.util.Map;

public class HandleAlreadyRegistered extends BaseClientFisException {
    public HandleAlreadyRegistered(String handle) {
        super(AllClientErrorCodes.HANDLE_ALREADY_REGISTERED, "Handle already registered: " + handle, Map.of("handle", handle));
    }
}
