package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_TABLE_INVALID_MIGRATION, detailFields = {"tableUuid", "reason"})
public class ServiceTableInvalidMigration extends BaseClientFisException {
    public ServiceTableInvalidMigration(String tableUuid, String reason) {
        super("Service Table with UUID \"" + tableUuid + "\" has invalid migration: " + reason, Map.of("tableUuid", tableUuid, "reason", reason));
    }
}