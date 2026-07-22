package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_TABLE_QUERY_INVALID, detailFields = {"reason"})
public class ServiceTableQueryInvalid extends BaseClientFisException {
    public ServiceTableQueryInvalid(String reason) {
        super("Service Table Query is invalid: " + reason, Map.of("reason", reason));
    }
}