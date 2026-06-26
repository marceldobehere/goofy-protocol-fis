package com.masl.goofy_protocol_fis_be.auth;

import lombok.Data;

@Data
public class RequestSignature {
    private String publicKey;
    private String signature;

    private Long reqId;
    private Long validUntil;

    private byte[] pathHash;
    private byte[] bodyHash;

    public RequestSignature(Long reqId, Long validUntil, String publicKey, String signature, byte[] bodyHash) {

    }
}