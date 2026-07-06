package com.masl.goofy_protocol_fis_be.exception.client;

import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_fis_be.exception.base.BaseClientFisException;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisHttpErrorCode;

import java.util.Map;

@FisHttpErrorCode(errorCode = AllClientErrorCodes.INVALID_SIGNATURE, detailFields = {"validity"})
public class InvalidSignatureException extends BaseClientFisException {
    public InvalidSignatureException(SignedRequest.SignedRequestValidity signedRequestValidity) {
        super("Invalid signature: " + signedRequestValidity, Map.of("validity", signedRequestValidity.name()));
    }
}
