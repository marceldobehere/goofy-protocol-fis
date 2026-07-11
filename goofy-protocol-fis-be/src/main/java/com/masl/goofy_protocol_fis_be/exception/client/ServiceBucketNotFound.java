package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_BUCKET_NOT_FOUND, detailFields = {"fileUuid"})
public class ServiceBucketNotFound extends BaseClientFisException {
    public ServiceBucketNotFound(String fileUuid) {
        super("Service Bucket with File UUID \"" + fileUuid + "\" not found", Map.of("fileUuid", fileUuid));
    }
}