package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_TABLE_INSERT_ENTRY_INVALID, detailFields = {"reason"})
public class ServiceTableInsertEntryInvalid extends BaseClientFisException {
    public ServiceTableInsertEntryInvalid(String reason) {
        super("Service Table Insert Entry is invalid: " + reason, Map.of("reason", reason));
    }
}