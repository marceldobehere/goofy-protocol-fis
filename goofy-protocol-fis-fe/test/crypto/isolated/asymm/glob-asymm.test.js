import {describe, it, expect} from 'vitest'
import {GlobAsymmCrypto} from "../../../../src/libs/goofy-protocol-core/crypto/isolated/asymm/glob-asymm-crypto.mjs";
const crypto = new GlobAsymmCrypto();

const testMessageStr = "This is a very crazy amazing test message";
const testMessageBytes = new Uint8Array([1, 2, 3, 10, 20, 30, 9, 10, 11, 0, 100, 127, 255, 156, 128, 123]);

function allTypes() {
    return crypto.getTypes();
}

function cryptoTypeAndSizes() {
    const sizes = [0, 1, 10, 100, 200, 1000, 10000, 100000, 1000000, 3000000];
    const types = allTypes();
    const out = [];
    for (const type of types) for (const size of sizes) out.push([type, size]);
    return out;
}

function randomBytes(size) {
    const arr = new Uint8Array(size);

    // random 32-bit seed (fast; not crypto-secure)
    let x = (Math.random() * 0x100000000) >>> 0;

    // xorshift32 PRNG (fast; not cryptographically secure)
    for (let i = 0; i < size; i++) {
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        arr[i] = x & 0xff;
    }

    return arr;
}

describe("Global Asymm Functions", () => {
    for (const type of allTypes()) {
        it(`Keygen & public split check (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const pubBytes = keypair.pub.serialize();
            const ok = await crypto.checkPublicSplitKey(pubBytes);
            expect(ok).toBe(true);
        });

        it(`Raw enc/dec roundtrip (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const enc = await crypto.encryptRaw(testMessageBytes, keypair.pub.serialize());
            expect(enc).toBeDefined();

            const dec = await crypto.decryptRaw(enc, keypair.priv.serialize());
            expect(dec).toBeDefined();

            // Compare Uint8Arrays
            expect(dec).not.toEqual(enc);
            expect(Array.from(dec)).toEqual(Array.from(testMessageBytes));
        });

        it(`Encoded enc/dec roundtrip (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const enc = await crypto.encrypt(testMessageBytes, keypair.pub.serialize());
            expect(enc).toBeDefined();

            const dec = await crypto.decrypt(enc, keypair.priv.serialize());
            expect(dec).toBeDefined();
            expect(Array.from(dec)).toEqual(Array.from(testMessageBytes));
        });

        it(`String enc/dec roundtrip (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const enc = await crypto.encryptStr(testMessageStr, keypair.pub.serialize());
            expect(enc).toBeDefined();

            const dec = await crypto.decryptStr(enc, keypair.priv.serialize());
            expect(dec).toBeDefined();
            expect(dec).not.toEqual(enc);

            expect(dec).toEqual(testMessageStr);
        });

        it(`Raw sign/verify (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const sig = await crypto.signRaw(testMessageBytes, keypair.priv.serialize());
            expect(sig).toBeDefined();

            const valid = await crypto.verifyRaw(testMessageBytes, sig, keypair.pub.serialize());
            expect(valid).toBe(true);
        });

        it(`Encoded sign/verify (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const sig = await crypto.sign(testMessageBytes, keypair.priv.serialize());
            expect(sig).toBeDefined();

            const valid = await crypto.verify(testMessageBytes, sig, keypair.pub.serialize());
            expect(valid).toBe(true);
        });

        it(`String sign/verify (type=${type.name})`, async () => {
            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const sig = await crypto.signStr(testMessageStr, keypair.priv.serialize());
            expect(sig).toBeDefined();

            const valid = await crypto.verifyStr(testMessageStr, sig, keypair.pub.serialize());
            expect(valid).toBe(true);
        });
    }

    for (const [type, size] of cryptoTypeAndSizes()) {
        it(`Raw enc/dec roundtrip with sizes (type=${type.name}, size=${size})`, async () => {
            const data = randomBytes(size);

            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const enc = await crypto.encryptRaw(data, keypair.pub.serialize());
            expect(enc).toBeDefined();

            const dec = await crypto.decryptRaw(enc, keypair.priv.serialize());
            expect(dec).toBeDefined();

            expect(Array.from(dec)).toEqual(Array.from(data));
        });

        it(`Raw sign/verify with sizes (type=${type.name}, size=${size})`, async () => {
            const data = randomBytes(size);

            const keypair = await crypto.generateKeypair(type);
            expect(keypair).toBeDefined();

            const sig = await crypto.signRaw(data, keypair.priv.serialize());
            expect(sig).toBeDefined();

            const valid = await crypto.verifyRaw(data, sig, keypair.pub.serialize());
            expect(valid).toBe(true);
        });
    }
});