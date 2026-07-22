package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_TABLE_QUOTA_EXCEEDED, detailFields = {"quotaName"}, description = "Service Table Entry had Quota exceeded! QuotaNames: `tableMaxDbSize`, `tableMaxFieldSize`, `tableMaxTables`, `tableMaxCols`, `tableMaxRows`, `tableMaxPermissionCount`, `generalMaxNameSize`, `tableQueryMaxResultCount`, `tableQueryMaxConditionCount`, `tableQueryMaxQueryLength`")
public class ServiceTableQuotaExceeded extends BaseClientFisException {
    public ServiceTableQuotaExceeded(String quotaName) {
        super("Service Table Entry had Quota \"" + quotaName + "\" exceeded!", Map.of("quotaName", quotaName));
    }
}