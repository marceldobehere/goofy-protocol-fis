package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_ENTRY_PATH_INVALID)
public class ServiceEntryPathInvalid extends BaseClientFisException {
    public ServiceEntryPathInvalid() {
        super("Service Entry Path is not Valid! It should have the format [id_handle]+[service_uuid] (Example: blah_blah_blah1234+23a4-1234-4bca-41fb)");
    }
}
