package com.masl.goofy_protocol_fis_be.exception.server;

import com.masl.goofy_protocol_fis_be.exception.base.BaseServerFisException;

import java.util.Map;

public class PublicKeyLookupFailed extends BaseServerFisException {
    public PublicKeyLookupFailed(String handle) {
        super(AllServerErrorCodes.PUBLIC_KEY_LOOKUP_FAILED, "Public key lookup failed for handle: " + handle, Map.of("handle", handle));
    }
}
