import { describe, it, expect, beforeEach } from "vitest";

import { GlobAsymmCrypto } from "../../../libs/goofy-protocol-core/crypto/isolated/asymm/glob-asymm-crypto.mjs";
import { HandleCrypto } from "../../../libs/goofy-protocol-core/crypto/connected/handle-crypto.mjs";
import { SignedRequest } from "../../../libs/goofy-protocol-core/crypto/connected/request/signed-request.mjs";
import { SampleHandleHelper } from "../../../libs/goofy-protocol-core/crypto/connected/sample-handle-helper.mjs";
import { BasicRequestValidator } from "../../../libs/goofy-protocol-core/crypto/connected/request/basic-request-validator.mjs"; // adjust path

const crypto = new GlobAsymmCrypto();

// Helpers
const DEF_DOMAIN = "rocc.systems";
const DEF_METHOD = "GET";
const DEF_PATH = "/api/test";
const DEF_BODY = new TextEncoder().encode("abcde12345".repeat(100000));

const knownHeaders = new Map([
    ["X-Goofy-Public-Key", "PUB.EC_C25519.MCowBQYDK2VwAyEAUjDbklJIC-tS6DWPl50vl_jYh5pxx68rHCQ89gpEWWA=.MCowBQYDK2VuAyEAuaDP2Gxo36baT2Hl6M4vo3vG4wlku1Lud71pSHfO22U=.gFAbQl8p8Ln0igp0sl71UFJIjmeBCS5EP2De_2m7TSKdfOTd6IhlQAp6E0Ql87BZbR2e5iFp4qsvlpsfROxxDQ=="],
    ["X-Goofy-Signature", "2nGWawycJfKAkmJMHyj8nzEdKQ86RT3iNFinWAsbPVh-0LTBeC1ZZzE7FLpnhr4ANIhhvVWmQmAVboyO1feRDg=="],
    ["X-Goofy-Valid-Until", "1782942585680"],
    ["X-Goofy-Id", "-8979107366769658500"],
]);

function headersToSize(headers) {
    let sum = 0;
    for (const [k, v] of headers.entries()) sum += k.length + v.length;
    return sum;
}

function checkFields(original, check) {
    expect(original.pubSplitKey).toBe(check.pubSplitKey);
    expect(original.handle).toBe(check.handle);
    expect(original.signature).toBe(check.signature);
    expect(original.uniqueId).toBe(check.uniqueId);
    expect(original.validUntil.getTime()).toBe(check.validUntil.getTime()); // mirrors rounded comparison in spirit
    expect(original.method).toBe(check.method);

    expect(Array.from(original.pathHash)).toEqual(Array.from(check.pathHash));
    expect(Array.from(original.bodyHash)).toEqual(Array.from(check.bodyHash));
}

// Collect all crypto types once
const allTypes = crypto.getTypes();

describe("SignedRequestTests", () => {
    let handleCrypto;
    const basicValidator = new BasicRequestValidator();

    beforeEach(() => {
        handleCrypto = new HandleCrypto(new SampleHandleHelper());
    });

    for (const type of allTypes) {
        it(`Create & Verify SignedRequest (type=${type.name}) (Public Key, No Body)`, async () => {
            const keypair = await crypto.generateKeypair(type);

            const req = await SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, null, handleCrypto);

            const valid = await req.isValidAsync(handleCrypto, basicValidator);
            expect(valid).toBe(SignedRequest.SignedRequestValidity.VALID);

            const headers = await req.toHeadersWithPubKey();
            expect(SignedRequest.hasAllRequestHeaders(headers)).toBe(true);
            expect(headersToSize(headers)).toBeGreaterThan(0);

            const reconstructed = await SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, null, handleCrypto);

            checkFields(req, reconstructed);

            const reconstructedValid = await reconstructed.isValidAsync(handleCrypto, basicValidator);
            expect(reconstructedValid).toBe(SignedRequest.SignedRequestValidity.VALID);
        });

        it(`Create & Verify SignedRequest (type=${type.name}) (Public Key, With Body)`, async () => {
            const keypair = await crypto.generateKeypair(type);

            const req = await SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

            const valid = await req.isValidAsync(handleCrypto, basicValidator);
            expect(valid).toBe(SignedRequest.SignedRequestValidity.VALID);

            const headers = await req.toHeadersWithPubKey();
            expect(SignedRequest.hasAllRequestHeaders(headers)).toBe(true);

            const reconstructed = await SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

            checkFields(req, reconstructed);

            const reconstructedValid = await reconstructed.isValidAsync(handleCrypto, basicValidator);
            expect(reconstructedValid).toBe(SignedRequest.SignedRequestValidity.VALID);
        });

        it(`Create & Verify SignedRequest (type=${type.name}) (Handle)`, async () => {
            const keypair = await crypto.generateKeypair(type);

            const req = await SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

            const valid = await req.isValidAsync(handleCrypto, basicValidator);
            expect(valid).toBe(SignedRequest.SignedRequestValidity.VALID);

            const headers = await req.toHeadersWithHandle();
            expect(SignedRequest.hasAllRequestHeaders(headers)).toBe(true);

            // Fresh HandleCrypto -> should fail
            const freshHandleCrypto = new HandleCrypto(new SampleHandleHelper());

            await expect(() =>
                SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, freshHandleCrypto)
            ).rejects.toBeDefined();

            // Cached HandleCrypto -> should pass
            const reconstructed = await SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

            checkFields(req, reconstructed);

            const reconstructedValid = await reconstructed.isValidAsync(handleCrypto, basicValidator);
            expect(reconstructedValid).toBe(SignedRequest.SignedRequestValidity.VALID);
        });

        it(`Check Domain Stripping of Handle (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);

            const req = await SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

            const headers = await req.toHeadersWithHandle(DEF_DOMAIN);
            expect(SignedRequest.hasAllRequestHeaders(headers)).toBe(true);

            expect(headers.get("X-Goofy-Handle")).toContain(DEF_DOMAIN);

            const freshHandleCrypto = new HandleCrypto(new SampleHandleHelper());

            await expect(() =>
                SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, freshHandleCrypto)
            ).rejects.toBeDefined();

            const reconstructed = await SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

            checkFields(req, reconstructed);

            const reconstructedValid = await reconstructed.isValidAsync(handleCrypto, basicValidator);
            expect(reconstructedValid).toBe(SignedRequest.SignedRequestValidity.VALID);
        });

        it(`Check Mocked Public Key Lookup from Handle (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            const actualHandle = await handleCrypto.deriveHandle(keypair.pub.serialize());

            const freshHandleCrypto = {
                getPublicSplitKeyFromHandle: async (handle) => (handle === actualHandle + "@" + DEF_DOMAIN) ? keypair.pub.serialize() : null,
                deriveHandle: async () => null,
            };

            const req = await SignedRequest.fromParts(keypair, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

            const headers = await req.toHeadersWithHandle(DEF_DOMAIN);
            expect(SignedRequest.hasAllRequestHeaders(headers)).toBe(true);

            expect(headers.get("X-Goofy-Handle")).toContain(DEF_DOMAIN);

            const reconstructed = await SignedRequest.fromRequestHeaders(headers, DEF_METHOD, DEF_PATH, DEF_BODY, freshHandleCrypto);

            checkFields(req, reconstructed);

            const reconstructedValid = await reconstructed.isValidAsync(handleCrypto, basicValidator);
            expect(reconstructedValid).toBe(SignedRequest.SignedRequestValidity.VALID);
        });
    }

    it("testKnownSignedRequestShouldHaveInvalidTime", async () => {
        const reconstructed = await SignedRequest.fromRequestHeaders(knownHeaders, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

        expect(SignedRequest.hasAllRequestHeaders(knownHeaders)).toBe(true);
        expect(headersToSize(knownHeaders)).toBeGreaterThan(0);

        const valid = await reconstructed.isValidAsync(handleCrypto, basicValidator);
        expect(valid).toBe(SignedRequest.SignedRequestValidity.INVALID_TIME);
    });

    it("testKnownSignedRequestMockedTimeShouldBeValid", async () => {
        const validator = new BasicRequestValidator();
        // mock only isValidUntilValid -> always true
        validator.isValidUntilValid = () => true;

        const reconstructed = await SignedRequest.fromRequestHeaders(knownHeaders, DEF_METHOD, DEF_PATH, DEF_BODY, handleCrypto);

        expect(SignedRequest.hasAllRequestHeaders(knownHeaders)).toBe(true);
        expect(headersToSize(knownHeaders)).toBeGreaterThan(0);

        const valid = await reconstructed.isValidAsync(handleCrypto, validator);
        expect(valid).toBe(SignedRequest.SignedRequestValidity.VALID);
    });
});