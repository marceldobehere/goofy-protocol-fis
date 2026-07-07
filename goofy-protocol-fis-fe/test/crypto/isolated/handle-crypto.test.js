import { describe, it, expect } from "vitest";
import { GlobAsymmCrypto } from "../../../src/libs/goofy-protocol-core/crypto/isolated/asymm/glob-asymm-crypto.mjs";
import { HandleCrypto } from "../../../src/libs/goofy-protocol-core/crypto/connected/handle-crypto.mjs";
import { SampleHandleHelper } from "../../../src/libs/goofy-protocol-core/crypto/connected/sample-handle-helper.mjs";
import { AsymmPubKeyPair } from "../../../src/libs/goofy-protocol-core/crypto/isolated/asymm/asymm-crypto-interface.mjs";


const crypto = new GlobAsymmCrypto();

const knownPubSplitKey = "PUB.RSA_2048.MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtUCkjjEAtRNDTMCN1DN0DpsLUXX1ll4dDd2wjjcuz8mx3re5mEmMIzahLACLA04TIxsHppA-WmwflKeWbJNdwUs6Th4JOHxRgRfsJLfk8fGUh_a_KYyBKNGHxR2dKdH5J25Kr7MKxeJUW6zAxVTozJh3Nhb38d2zhncz2D5hUpBWR-WfPUrjYPTwPp6ysHz_snG6ud5AuimfpblLLfBce9OixpPNNl7n9CCtmZfV7JDVwUNzPC3DJH6x49Zzf1Pn9AS2OOqSHmPGcM_irrEdXe7sP7-MVKLG9g67N23zg58RjVduFqUVt4TPGLCO9vjueBaKGhswT_p2NNR3M-IgWwIDAQAB.X.X";
const knownPubSplitKeyHandle = "kimet_grill_agger_pyets50302";

function allTypes() {
    return crypto.getTypes();
}

describe("HandleCrypto Tests", () => {
    const handleCrypto = new HandleCrypto(new SampleHandleHelper());

    for (const type of allTypes()) {
        it(`Keygen & handle derivation (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const pubSerialized = keypair.pub.serialize();
            const ok = await crypto.checkPublicSplitKey(pubSerialized);
            expect(ok).toBe(true);

            const handle = await handleCrypto.deriveHandle(pubSerialized);
            expect(handle).toBeDefined();
            expect(await handleCrypto.verifyKeyAndHandle(pubSerialized, handle)).toBe(true);
        });

        it(`Deterministic Keygen & handle derivation (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const serialized = keypair.pub.serialize();

            const handle1 = await handleCrypto.deriveHandle(serialized);
            expect(handle1).toBeDefined();
            expect(await handleCrypto.verifyKeyAndHandle(serialized, handle1)).toBe(true);

            const handle2 = await handleCrypto.deriveHandle(AsymmPubKeyPair.parse(serialized));
            expect(handle1).toEqual(handle2);
        });

        it(`Keygen & handle derivation + (cached) lookup (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const pubSerialized = keypair.pub.serialize();
            const ok = await crypto.checkPublicSplitKey(pubSerialized);
            expect(ok).toBe(true);

            const handle = await handleCrypto.deriveHandle(pubSerialized);
            expect(handle).toBeDefined();
            expect(await handleCrypto.verifyKeyAndHandle(pubSerialized, handle)).toBe(true);

            const pubSplitKey = await handleCrypto.getPublicSplitKeyFromHandle(handle);
            expect(pubSplitKey).toBeDefined();
            expect(pubSplitKey).toEqual(pubSerialized);
        });

        it(`Keygen & unknown lookup (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const pubSerialized = keypair.pub.serialize();
            const ok = await crypto.checkPublicSplitKey(pubSerialized);
            expect(ok).toBe(true);

            // Derive without interacting with the cache
            const handle = await handleCrypto._internalDeriveHandle(pubSerialized);
            expect(handle).toBeDefined();
            expect(await handleCrypto.verifyKeyAndHandle(pubSerialized, handle, false)).toBe(true);

            // Lookup should not be found
            const pubSplitKey = await handleCrypto.getPublicSplitKeyFromHandle(handle);
            expect(pubSplitKey).toBeNull();
        });
    }

    it("Known handle derivation", async () => {
        const handle = await handleCrypto.deriveHandle(knownPubSplitKey);
        expect(handle).toBeDefined();
        expect(handle).toEqual(knownPubSplitKeyHandle);
    });
});