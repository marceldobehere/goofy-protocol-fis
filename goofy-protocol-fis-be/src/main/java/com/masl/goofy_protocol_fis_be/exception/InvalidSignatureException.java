package com.masl.goofy_protocol_fis_be.exception;

import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;

public class InvalidSignatureException extends RuntimeException {
    public InvalidSignatureException(SignedRequest.SignedRequestValidity signedRequestValidity) {
        super("Invalid signature: " + signedRequestValidity);
    }
}
