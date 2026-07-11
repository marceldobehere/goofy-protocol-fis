package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_BUCKET_PERMS_INVALID)
public class ServiceBucketPermsInvalid extends BaseClientFisException {
    public ServiceBucketPermsInvalid() {
        super("Service Bucket Permissions are invalid");
    }
}
