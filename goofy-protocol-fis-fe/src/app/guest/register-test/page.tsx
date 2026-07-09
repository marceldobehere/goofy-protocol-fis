'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {
    asymmDecryptObj, asymmEncryptObj, asymmSignObj, asymmVerifyObj, createSignedRequest,
    deriveHandleFromPublicSplitKey, generateAsymmKeypair,
    getHeadersFromSignedRequestWithHandle, getHeadersFromSignedRequestWithPubkey
} from "@/libs/crypto";
import {AsymmCryptoType} from "@/libs/crypto-types";
import {GeneralInfoDto, RequestError, RequestFisError} from "@/libs/dtos";
import {getAuth, getNoAuth} from "@/libs/req";
import {hasKeypair, saveKeypair} from "@/libs/auth-store";

export default function Page() {

    async function test() {
        const keypair = await generateAsymmKeypair(AsymmCryptoType.EC_C25519);
        console.log(keypair);
        console.log("Pub:", keypair.pub.serialize());
        console.log("Priv:", keypair.priv.serialize());

        const handle = await deriveHandleFromPublicSplitKey(keypair.pub);
        console.log("Handle:", handle);

        const testObj = {
            "lol": 123,
            "bruh": "lol"
        };
        console.log("Obj:", testObj);

        const sig = await asymmSignObj(testObj, keypair.priv);
        console.log("Sig:", sig);

        const check = await asymmVerifyObj(testObj, sig, keypair.pub);
        console.log("Check:", check);

        const enc = await asymmEncryptObj(testObj, keypair.pub);
        console.log("Enc:", enc);

        const dec = await asymmDecryptObj(enc, keypair.priv);
        console.log("Dec:", dec);

        const req = await createSignedRequest(keypair, "GET", "/test", "lol");
        console.log("req:", req);

        const headers1 = getHeadersFromSignedRequestWithPubkey(req);
        console.log("headers1:", headers1);

        const headers2 = getHeadersFromSignedRequestWithHandle(req);
        console.log("headers2:", headers2);

        const headers3 = getHeadersFromSignedRequestWithHandle(req, "test.com");
        console.log("headers3:", headers3);

    }

    async function testRequest() {
        if (!(await hasKeypair())) {
            const keypair = await generateAsymmKeypair(AsymmCryptoType.EC_C25519);
            await saveKeypair(keypair);
        }

        // Should work and return DTO
        const generalInfo: GeneralInfoDto = await getNoAuth("/api/general/info");
        console.log("generalInfo:", generalInfo);

        // Should work
        try {
            const outsiderTest: string = await getAuth("/api/test/test-outsider");
            console.log("outsiderTest:", outsiderTest);
        } catch (error) {
            if (error instanceof RequestFisError)
                console.debug("Outsider RequestFisError:", error.toString());
            else if (error instanceof RequestError)
                console.debug("Outsider RequestError:", error.toString());
            else
                console.debug(error);
        }

        // Should Fail with Access Denied
        try {
            const userTest: string = await getAuth("/api/test/test-user");
            console.log("userTest:", userTest);
        } catch (error) {
            if (error instanceof RequestFisError)
                console.debug("User RequestFisError:", error.toString());
            else if (error instanceof RequestError)
                console.debug("User RequestError:", error.toString());
            else
                console.debug(error);
        }
    }

    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Register</h2>

                <p className={styles.Introduction}>
                    <button  onClick={test}>Test Local</button>
                    &#32;
                    <button  onClick={testRequest}>Test Request</button>
                </p>


                <div className={styles.MainButtons}>
                    <Link href={"/guest/login"}>Login</Link>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
