package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.SERVICE_ENTRY_INVALID)
public class ServiceEntryInvalid extends BaseClientFisException {
    public ServiceEntryInvalid() {
        super("Service Entry is not Valid");
    }
}
