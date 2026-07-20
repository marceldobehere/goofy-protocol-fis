package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_TABLE_NOT_FOUND, detailFields = {"tableUuid"})
public class ServiceTableNotFound extends BaseClientFisException {
    public ServiceTableNotFound(String tableUuid) {
        super("Service Table with UUID \"" + tableUuid + "\" not found", Map.of("tableUuid", tableUuid));
    }
}