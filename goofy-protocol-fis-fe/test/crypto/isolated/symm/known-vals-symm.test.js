import { describe, it, expect } from "vitest";
import { GlobSymmCrypto } from "../../../../src/libs/goofy-protocol-core/crypto/isolated/symm/glob-symm-crypto.mjs";
import { SymmGlobKnownValues } from "./known-vals-symm.mjs";

const crypto = new GlobSymmCrypto();

const knownValueEntries =
    SymmGlobKnownValues.knownValues instanceof Map
        ? Array.from(SymmGlobKnownValues.knownValues.entries())
        : Array.from(SymmGlobKnownValues.knownValues);

describe("SymmGlobKnownTests", () => {
    for (const [type, set] of knownValueEntries) {
        it(`Global Symmetric Decryption using known values (${type.name ?? String(type)})`, async () => {
            const strDec = await crypto.decryptStr(set.strEnc, set.secret);
            const rawDec = await crypto.decryptRaw(set.rawEnc, set.secret);
            const defDec = await crypto.decrypt(set.defEnc, set.secret);

            expect(strDec).toBe(set.strOg);
            expect(Array.from(rawDec)).toEqual(Array.from(set.rawDefOg));
            expect(Array.from(defDec)).toEqual(Array.from(set.rawDefOg));
        });

        it(`Global Symmetric Encryption using known values (${type.name ?? String(type)})`, async () => {
            const strEnc = await crypto.encryptStr(set.strOg, set.secret);
            const rawEnc = await crypto.encryptRaw(set.rawDefOg, set.secret);
            const defEnc = await crypto.encrypt(set.rawDefOg, set.secret);

            expect(strEnc).not.toBeNull();
            expect(rawEnc).not.toBeNull();
            expect(defEnc).not.toBeNull();

            const strDec = await crypto.decryptStr(strEnc, set.secret);
            const rawDec = await crypto.decryptRaw(rawEnc, set.secret);
            const defDec = await crypto.decrypt(defEnc, set.secret);

            expect(strDec).toBe(set.strOg);
            expect(Array.from(rawDec)).toEqual(Array.from(set.rawDefOg));
            expect(Array.from(defDec)).toEqual(Array.from(set.rawDefOg));
        });
    }
});