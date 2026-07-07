'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {
    asymmDecryptObj, asymmEncryptObj, asymmSignObj, asymmVerifyObj, createSignedRequest,
    deriveHandleFromPublicSplitKey, generateAsymmKeypair,
    getHeadersFromSignedRequestWithHandle, getHeadersFromSignedRequestWithPubkey
} from "@/libs/crypto";
import {AsymmCryptoType} from "@/libs/crypto-types";

export default function Home() {

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

    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Register</h2>

                <p className={styles.Introduction}>
                    <button  onClick={test}>Test</button>
                </p>


                <div className={styles.MainButtons}>
                    <Link href={"/guest/login"}>Login</Link>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
