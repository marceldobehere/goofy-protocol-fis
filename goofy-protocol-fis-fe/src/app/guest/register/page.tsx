'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {
    asymmDecryptObj, asymmEncryptObj, asymmSignObj, asymmVerifyObj, createSignedRequest,
    deriveHandleFromPublicSplitKey, generateAsymmKeypair,
    getHeadersFromSignedRequestWithHandle, getHeadersFromSignedRequestWithPubkey
} from "@/libs/crypto";
import {AsymmCryptoType, AsymmFullKeyPair} from "@/libs/crypto-types";
import {GeneralInfoDto, RequestError, RequestFisError} from "@/libs/dtos";
import {getAuth, getNoAuth} from "@/libs/req";
import {hasKeypair, saveKeypair} from "@/libs/auth-store";
import {useEffect, useState} from "react";

export default function Page() {
    const [currStep, setCurrStep] = useState<number>(0);
    const [currKeypair, setCurrKeypair] = useState<AsymmFullKeyPair | null>(null);
    const [currHandle, setCurrHandle] = useState<string | null>(null);
    const [exported, setExported] = useState<boolean>(false);

    const [selectedType, setSelectedType] = useState<AsymmCryptoType | null>(null);
    const [registerCode, setRegisterCode] = useState<string>("");

    useEffect(() => {
        if (currStep == 0 && currKeypair == null)
            generateNewKeypair().then();
    })

    async function generateNewKeypair() {
        // TODO: Check Type and use it and alert if not selected

        const keypair = await generateAsymmKeypair(AsymmCryptoType.EC_C25519);
        const handle = await deriveHandleFromPublicSplitKey(keypair.pub);
        setCurrKeypair(keypair);
        setCurrHandle(handle);
    }

    async function requestRegisterCode() {
        // TODO: Implement
    }



    async function _importKeypair(): Promise<AsymmFullKeyPair | null> {
        // TODO: Implement
        return null;
    }

    async function importKeypair() {
        const keypair = await _importKeypair();
        const handle = keypair == null ? null : await deriveHandleFromPublicSplitKey(keypair.pub);
        setCurrKeypair(keypair);
        setCurrHandle(handle);
    }

    async function exportKeypair() {
        // TODO: Implement
        setExported(true);
    }






    async function backToFirstStep() {
        setCurrStep(0);
    }

    async function continueFirstStep() {
        if (!exported) {
            alert("Please export the Keypair first!")
            return;
        }

        // TODO: Check if Register Code is valid first

        setCurrKeypair(null);
        setCurrHandle(null);
        setCurrStep(1);
    }

    async function toLastStep() {
        setCurrStep(2);
    }

    async function complete() {
        // TODO: Redirect to Home Page
    }

    // TODO: Use different variables for Step 2 and dont reset them, better UX
    // TODO: Disable the buttons when needed
    // TODO: Enable pasting in the Public / Private Key and dynamically derive Handle
    // TODO: Setup preferences (LocalStorage / Session Storage + Username/Password Login?)
    // TODO: Add better explanations
    // TODO: Error Handling
    // TODO: Styling
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Register</h2>

                {currStep === 0 ? (<>
                    <div>
                        <h3>Step 1: Setup Identity</h3>
                        <p>
                            Please generate/import a keypair to be used as your identity and enter a valid register code.<br/>
                            If you don&apos;t have a register code, you can request one by pressing the button below.
                        </p>

                        <br/>
                        <input type={"text"}     readOnly={true} placeholder={"Public Key"}  value={currKeypair?.pub.serialize() || ""}></input><br/>
                        <input type={"password"} readOnly={true} placeholder={"Private Key"} value={currKeypair?.priv.serialize() || ""}></input><br/>
                        <input type={"text"}     readOnly={true} placeholder={"Handle"}      value={currHandle || ""}></input><br/>

                        <br/>
                        <label>Enter Register Code:</label><br/>
                        <input type={"text"} placeholder={"Register Code"}></input><br/>

                        <br/>
                        <button onClick={generateNewKeypair}>Generate Keypair</button><br/>
                        <button onClick={importKeypair}>Import Keypair</button><br/>
                        <button onClick={exportKeypair}>Export Keypair</button><br/>
                        <button onClick={requestRegisterCode}>Request a Register Code</button><br/>

                        <br/>
                        <button onClick={continueFirstStep}>Continue</button><br/>
                    </div>
                </>) : (<></>)}

                {currStep === 1 ? (<>
                    <div>
                        <h3>Step 2: Check Keypair</h3>
                        <p>To finish your registration, please prove that you actually downloaded your keypair by entering/importing it here.</p>

                        <br/>
                        <input type={"text"}     disabled placeholder={"Public Key"}  value={currKeypair?.pub.serialize() || ""}></input><br/>
                        <input type={"password"} disabled placeholder={"Private Key"} value={currKeypair?.priv.serialize() || ""}></input><br/>
                        <input type={"text"}     readOnly={true} placeholder={"Handle"}      value={currHandle || ""}></input><br/>


                        <br/>
                        <button onClick={importKeypair}>Import Keypair</button><br/>

                        <br/>
                        <button onClick={toLastStep}>Register</button><br/>
                        <button onClick={backToFirstStep}>Back</button><br/>
                    </div>
                </>) : (<></>)}

                {currStep === 2 ? (<>
                    <div>
                        <h3>Step 3: Preferences</h3>
                        <p>You are now registered! To finish up, please set up your preferences!</p>

                        <br/>
                        <label>Your Handle: </label>
                        <input type={"text"}     readOnly={true} placeholder={"Handle"}      value={currHandle || ""}></input><br/>


                        <br/>
                        <button onClick={complete}>Complete</button><br/>
                    </div>
                </>) : (<></>)}


                <div className={styles.MainButtons}>
                    <Link href={"/guest/login"}>Login</Link>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
