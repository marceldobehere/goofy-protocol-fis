package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;

import java.util.Map;

public class InvalidSignatureException extends BaseClientFisException {
    public InvalidSignatureException(SignedRequest.SignedRequestValidity signedRequestValidity) {
        super(AllClientErrorCodes.INVALID_SIGNATURE, "Invalid signature: " + signedRequestValidity, Map.of("validity", signedRequestValidity.name()));
    }
}
