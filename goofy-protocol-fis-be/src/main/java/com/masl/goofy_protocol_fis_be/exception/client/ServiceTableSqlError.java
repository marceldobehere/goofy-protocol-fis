package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_TABLE_SQL_ERROR, detailFields = {"tableUuid", "errorMessage"})
public class ServiceTableSqlError extends BaseClientFisException {
    public ServiceTableSqlError(String tableUuid, String errorMessage) {
        super("Service Table with UUID \"" + tableUuid + "\" encountered an SQL Error: " + errorMessage, Map.of("tableUuid", tableUuid, "errorMessage", errorMessage));
    }
}