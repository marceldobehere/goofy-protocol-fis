package com.masl.goofy_protocol_core.crypto.connected.request;

import com.masl.goofy_protocol_core.crypto.connected.GenericHandleCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.SecretUtils;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

// TODO: Write tests
public record SignedRequest(
        String pubSplitKey,
        String handle, // shouldn't contain domain but needs to be checked when parsing
        String signature,
        long uniqueId,
        Instant validUntil,
        String method,
        byte[] pathHash,
        byte[] bodyHash
) {
    public static final byte[] EMPTY_BODY_VAL = new byte[] { 123 };
    public static final int DEF_HASH_SIZE = 256 / 8;
    public static final String DEF_SEPARATOR = "#";
    public static final int DEF_MAX_VALIDITY = 30;

    private static GlobAsymmCrypto asymmCrypto = new GlobAsymmCrypto();

    public boolean isValid(GenericHandleCrypto handleCrypto, SignedRequestValidator validator) {
        if (handleCrypto == null || validator == null)
            return false;

        // Check general validity
        if (pubSplitKey == null || handle == null || signature == null || validUntil == null || method == null || pathHash == null || bodyHash == null)
            return false;
        if (pathHash.length != DEF_HASH_SIZE || bodyHash.length != DEF_HASH_SIZE)
            return false;

        // Check time based and unique validity
        if (!validator.isValidUntilValid(validUntil))
            return false;
        if (!validator.isUniqueIdValid(uniqueId))
            return false;

        // Check Public Key
        asymmCrypto.checkPublicSplitKey(pubSplitKey);

        // Check Handle against Public Key
        String derivedHandle = handleCrypto.deriveHandle(pubSplitKey);
        if (derivedHandle == null || !derivedHandle.equals(handle))
            return false;

        // Check Signature
        String baseObj =
                method + DEF_SEPARATOR +
                uniqueId + DEF_SEPARATOR +
                validUntil.toEpochMilli() + DEF_SEPARATOR +
                Base64.getUrlEncoder().encodeToString(pathHash) + DEF_SEPARATOR +
                Base64.getUrlEncoder().encodeToString(bodyHash);
        if (!asymmCrypto.verifyStr(baseObj, signature, pubSplitKey))
            return false;

        return true;
    }

    public static SignedRequest fromParts(AsymmCrypto.AsymmFullKeyPair keypair, String method, String path, byte[] body, GenericHandleCrypto handleCrypto) {
        // Calculate Hash
        byte[] pathHash = SecretUtils.sha256(path);
        byte[] bodyHash = SecretUtils.sha256(body);

        // Get Id and Time
        long uniqueId = new SecureRandom().nextLong();
        Instant validUntil = Instant.now().plusSeconds(DEF_MAX_VALIDITY);


        // Create Signature
        String baseObj =
                method + DEF_SEPARATOR +
                uniqueId + DEF_SEPARATOR +
                validUntil.toEpochMilli() + DEF_SEPARATOR +
                Base64.getUrlEncoder().encodeToString(pathHash) + DEF_SEPARATOR +
                Base64.getUrlEncoder().encodeToString(bodyHash);
        String sig = asymmCrypto.signStr(baseObj, keypair.priv().serialize());

        // Create Signed request
        return new SignedRequest(
                keypair.pub().serialize(),
                handleCrypto.deriveHandle(keypair.pub().serialize()),
                sig,
                uniqueId,
                validUntil,
                method,
                pathHash,
                bodyHash
        );
    }

    // TODO: Properly define and document
    // 'X-Goofy-Signature': encodeURIComponent(signature),
    // 'X-Goofy-Id': id,
    // 'X-Goofy-Valid-Until': validUntil,
    // 'X-Goofy-Public-Key': encodeURIComponent(publicKey),
    // 'X-Goofy-RAW': !!sendRawBody,

    public static SignedRequest fromRequestHeaders(Map<String, String> headers, String method, String path, byte[] body, GenericHandleCrypto handleCrypto) {
        // TODO: implement
        // TODO: Check if handle or public key is provided, either derive or lookup the other one
        // TODO: Check if handle contains domain, if yes remove
        return null;
    }

    private Map<String, String> toHeaders(boolean usePubKey, String optHandleDomain) {
        // TODO: implement
        return null;
    }


    public static SignedRequest fromRequestHeaders(Map<String, String> headers, String method, String path, GenericHandleCrypto handleCrypto) {return fromRequestHeaders(headers, method, path, EMPTY_BODY_VAL, handleCrypto);}
    public static SignedRequest fromParts(AsymmCrypto.AsymmFullKeyPair keypair, String method, String path, GenericHandleCrypto handleCrypto) {return fromParts(keypair, method, path, EMPTY_BODY_VAL, handleCrypto);}

    public Map<String, String> toHeadersWithPubKey() {return toHeaders(true, null);}
    public Map<String, String> toHeadersWithHandle() {return toHeaders(false, null);}
    public Map<String, String> toHeadersWithHandle(String optHandleDomain) {return toHeaders(false, optHandleDomain);}
}
