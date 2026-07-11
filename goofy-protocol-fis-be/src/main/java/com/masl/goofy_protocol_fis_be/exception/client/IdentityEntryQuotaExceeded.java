package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.IDENTITY_ENTRY_QUOTA_EXCEEDED, detailFields={"quota"})
public class IdentityEntryQuotaExceeded extends BaseClientFisException {
    public IdentityEntryQuotaExceeded(int quota) {
        super("Identity Entry Quota of " + quota + " exceeded!", Map.of("quota", quota));
    }
}
