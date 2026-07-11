package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.INVALID_PUBLIC_KEY, description = "This means that a Public Key is invalid.")
public class InvalidPublicKey extends BaseClientFisException {
    public InvalidPublicKey() {
        super("Invalid public key");
    }
}
