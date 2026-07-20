package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_TABLE_LOCK_INVALID, detailFields = {"tableUuid", "lockToken"})
public class ServiceTableLockInvalid extends BaseClientFisException {
    public ServiceTableLockInvalid(String tableUuid, String lockToken) {
        super("Lock Token for Service Table with UUID \"" + tableUuid + "\" invalid", Map.of("tableUuid", tableUuid, "lockToken", lockToken));
    }
}