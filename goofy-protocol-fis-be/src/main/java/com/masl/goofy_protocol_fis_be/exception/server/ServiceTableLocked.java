package com.masl.goofy_protocol_fis_be.exception.server;

import com.masl.goofy_protocol_fis_be.exception.base.BaseServerFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllServerErrorCodes.SERVICE_TABLE_LOCKED, detailFields = {"tableUuid"}, description = "This Error indicates that the Table is currently locked and can only be accessed with a Lock Token or once the Lock has been released / has expired. <br>The client is encouraged to retry the request after waiting a bit.")
public class ServiceTableLocked extends BaseServerFisException {
    public ServiceTableLocked(String tableUuid) {
        super("Table with UUID \"" + tableUuid + "\" is currently locked!", Map.of("tableUuid", tableUuid));
    }
}