package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.INVALID_SIGNED_OBJECT, description = "This means that a Signature for an Object is invalid.")
public class InvalidSignedObject extends BaseClientFisException {
    public InvalidSignedObject() {
        super("Invalid signature for object");
    }
}
