import {describe, it, expect} from 'vitest'
import { GlobAsymmCrypto } from "../../../../libs/goofy-protocol-core/crypto/isolated/asymm/glob-asymm-crypto.mjs";
import { AsymmGlobKnownValues } from "./known-vals-asymm.mjs";

const crypto = new GlobAsymmCrypto();

const knownValueEntries =
    AsymmGlobKnownValues.knownValues instanceof Map
        ? Array.from(AsymmGlobKnownValues.knownValues.entries())
        : Array.from(AsymmGlobKnownValues.knownValues);

describe("AsymmGlobKnownTests", () => {
    for (const [type, set] of knownValueEntries) {
        it(`Global Asymmetric Decryption using known values (${type.name ?? String(type)})`, async () => {
            // Check Public Keypair
            await crypto.checkPublicSplitKey(set.keypair.pub.serialize());

            // Run Decryption
            const strDec = await crypto.decryptStr(set.strEnc, set.keypair.priv.serialize());
            const rawDec = await crypto.decryptRaw(set.rawEnc, set.keypair.priv.serialize());
            const defDec = await crypto.decrypt(set.defEnc, set.keypair.priv.serialize());

            // Check Decryption
            expect(strDec).toBe(set.strOg);
            expect(Array.from(rawDec)).toEqual(Array.from(set.rawDefOg));
            expect(Array.from(defDec)).toEqual(Array.from(set.rawDefOg));
        });

        it(`Global Asymmetric Verification using known values (${type.name ?? String(type)})`, async () => {
            // Check Public Keypair
            await crypto.checkPublicSplitKey(set.keypair.pub.serialize());

            // Run Verification
            const strVer = await crypto.verifyStr(set.strOg, set.strSig, set.keypair.pub.serialize());
            const rawVer = await crypto.verifyRaw(set.rawDefOg, set.rawSig, set.keypair.pub.serialize());
            const defVer = await crypto.verify(set.rawDefOg, set.defSig, set.keypair.pub.serialize());

            // Check Verification
            expect(strVer).toBe(true);
            expect(rawVer).toBe(true);
            expect(defVer).toBe(true);
        });

        it(`Global Asymmetric Encryption using known values (${type.name ?? String(type)})`, async () => {
            // Check Public Keypair
            await crypto.checkPublicSplitKey(set.keypair.pub.serialize());

            // Run Encryption
            const strEnc = await crypto.encryptStr(set.strOg, set.keypair.pub.serialize());
            const rawEnc = await crypto.encryptRaw(set.rawDefOg, set.keypair.pub.serialize());
            const defEnc = await crypto.encrypt(set.rawDefOg, set.keypair.pub.serialize());

            // Check Encryption
            expect(strEnc).not.toBeNull();
            expect(rawEnc).not.toBeNull();
            expect(defEnc).not.toBeNull();

            // Run Decryption
            const strDec = await crypto.decryptStr(strEnc, set.keypair.priv.serialize());
            const rawDec = await crypto.decryptRaw(rawEnc, set.keypair.priv.serialize());
            const defDec = await crypto.decrypt(defEnc, set.keypair.priv.serialize());

            // Check Decryption
            expect(strDec).toBe(set.strOg);
            expect(Array.from(rawDec)).toEqual(Array.from(set.rawDefOg));
            expect(Array.from(defDec)).toEqual(Array.from(set.rawDefOg));
        });

        it(`Global Asymmetric Sign using known values (${type.name ?? String(type)})`, async () => {
            // Check Public Keypair
            await crypto.checkPublicSplitKey(set.keypair.pub.serialize());

            // Run Signing
            const strSig = await crypto.signStr(set.strOg, set.keypair.priv.serialize());
            const rawSig = await crypto.signRaw(set.rawDefOg, set.keypair.priv.serialize());
            const defSig = await crypto.sign(set.rawDefOg, set.keypair.priv.serialize());

            // Check Sign Outputs
            expect(strSig).not.toBeNull();
            expect(rawSig).not.toBeNull();
            expect(defSig).not.toBeNull();

            // Verify
            const strVer = await crypto.verifyStr(set.strOg, strSig, set.keypair.pub.serialize());
            const rawVer = await crypto.verifyRaw(set.rawDefOg, rawSig, set.keypair.pub.serialize());
            const defVer = await crypto.verify(set.rawDefOg, defSig, set.keypair.pub.serialize());

            expect(strVer).toBe(true);
            expect(rawVer).toBe(true);
            expect(defVer).toBe(true);
        });
    }
});