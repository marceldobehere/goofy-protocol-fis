package com.masl.goofy_protocol_fis_be.exception.server;

import com.masl.goofy_protocol_fis_be.exception.base.BaseServerFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllServerErrorCodes.PUBLIC_KEY_LOOKUP_FAILED)
public class PublicKeyLookupFailed extends BaseServerFisException {
    public PublicKeyLookupFailed(String handle) {
        super("Public key lookup failed for handle: " + handle, Map.of("handle", handle));
    }
}
