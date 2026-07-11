package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_ENTRY_NOT_FOUND, detailFields = {"uuid"})
public class ServiceEntryNotFound extends BaseClientFisException {
    public ServiceEntryNotFound(String uuid) {
        super("Service Entry for \"" + uuid + "\" not found", Map.of("uuid", uuid));
    }
}
