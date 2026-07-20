package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_TABLE_LOCK_REQUEST_INVALID, detailFields = {"tableUuid"})
public class ServiceTableLockRequestInvalid extends BaseClientFisException {
    public ServiceTableLockRequestInvalid(String tableUuid) {
        super("Lock Request for Service Table with UUID \"" + tableUuid + "\" invalid", Map.of("tableUuid", tableUuid));
    }
}