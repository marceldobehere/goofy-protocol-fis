package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_BUCKET_FILE_ERROR)
public class ServiceBucketFileError extends BaseClientFisException {
    public ServiceBucketFileError() {
        super("An Error with the Service Bucket File occured");
    }
}
