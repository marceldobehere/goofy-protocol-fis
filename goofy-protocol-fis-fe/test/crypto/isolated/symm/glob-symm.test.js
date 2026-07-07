import {describe, it, expect} from 'vitest'
import {GlobSymmCrypto} from "../../../../src/libs/goofy-protocol-core/crypto/isolated/symm/glob-symm-crypto.mjs";

const crypto = new GlobSymmCrypto();

const randomSecretBase = "bla bla bla randdom secret";
const testMessageStr = "This is a very crazy amazing test message";
const testMessageBytes = new Uint8Array([1, 2, 3, 10, 20, 30, 9, 10, 11, 0, 100, 127, 255, 156, 128, 123]); // -1,-100,-128 mapped to 255,156,128

function allTypes() {
    return crypto.getTypes();
}

function cryptoTypeAndSizes() {
    const sizes = [
        0, 1, 1_000, 10_000, 100_000, 1_000_000, 5_000_000
    ];
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

describe("Global Symm Functions", async () => {
    for (const type of allTypes()) {
        it(`Raw enc/dec roundtrip (type=${type.name})`, async () => {
            const randomSecret = randomSecretBase + type.toString();

            const enc = await crypto.encryptRaw(testMessageBytes, randomSecret, type);
            expect(enc).toBeDefined();

            const dec = await crypto.decryptRaw(enc, randomSecret);
            expect(dec).toBeDefined();

            expect(Array.from(dec)).toEqual(Array.from(testMessageBytes));
            expect(dec).not.toEqual(enc);
        });

        it(`Encoded enc/dec roundtrip (type=${type.name})`, async () => {
            const randomSecret = randomSecretBase + type.toString();

            const enc = await crypto.encrypt(testMessageBytes, randomSecret, type);
            expect(enc).toBeDefined();

            const dec = await crypto.decrypt(enc, randomSecret);
            expect(dec).toBeDefined();

            expect(Array.from(dec)).toEqual(Array.from(testMessageBytes));
        });

        it(`String enc/dec roundtrip (type=${type.name})`, async () => {
            const randomSecret = randomSecretBase + type.toString();

            const enc = await crypto.encryptStr(testMessageStr, randomSecret, type);
            expect(enc).toBeDefined();

            const dec = await crypto.decryptStr(enc, randomSecret);
            expect(dec).toBeDefined();

            expect(dec).toEqual(testMessageStr);
            expect(dec).not.toEqual(enc);
        });

        it(`Ciphertext differs across runs (type=${type.name})`, async () => {
            const randomSecret = randomSecretBase + type.toString();

            const enc1 = await crypto.encrypt(testMessageBytes, randomSecret, type);
            expect(enc1).toBeDefined();

            const enc2 = await crypto.encrypt(testMessageBytes, randomSecret, type);
            expect(enc2).toBeDefined();

            expect(enc1).not.toEqual(enc2);
        });
    }

    for (const [type, size] of cryptoTypeAndSizes()) {
        it(`Raw enc/dec roundtrip with sizes (type=${type.name}, size=${size})`, async () => {
            const data = randomBytes(size);
            const randomSecret = randomSecretBase + type.toString() + size;

            const enc = await crypto.encryptRaw(data, randomSecret, type);
            expect(enc).toBeDefined();

            const dec = await crypto.decryptRaw(enc, randomSecret);
            expect(dec).toBeDefined();

            expect(Array.from(dec)).toEqual(Array.from(data));
            expect(dec).not.toEqual(enc);
        });
    }
});