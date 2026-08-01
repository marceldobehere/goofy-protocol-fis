package com.masl.goofy_protocol_fis_be.exception.server;

import com.masl.goofy_protocol_fis_be.exception.base.BaseServerFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllServerErrorCodes.SERVICE_BUCKET_FILE_ERROR)
public class ServiceBucketFileError extends BaseServerFisException {
    public ServiceBucketFileError() {
        super("An Error with the Service Bucket File occurred");
    }
}
