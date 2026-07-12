package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_BUCKET_QUOTA_EXCEEDED, detailFields = {"quotaName"}, description = "Service Bucket Entry had Quota exceeded! QuotaNames: `maxItemSize`, `maxBucketSize`, `maxItemCount`")
public class ServiceBucketQuotaExceeded extends BaseClientFisException {
    public ServiceBucketQuotaExceeded(String quotaName) {
        super("Service Bucket Entry had Quota \"" + quotaName + "\" exceeded!", Map.of("quotaName", quotaName));
    }
}