'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {deriveHandleFromPublicSplitKey, serializeFullKeypair, generateAsymmKeypair,parseFullKeypair} from "@/libs/crypto";
import {AsymmCryptoType, AsymmFullJsonKeypair, AsymmFullKeyPair} from "@/libs/crypto-types";
import {RegistrationRequestDto} from "@/libs/dtos";
import {saveKeypair, setStorageMode} from "@/libs/auth-store";
import {useEffect, useState} from "react";
import {downloadObjFile, readJsonFile, uploadData} from "@/libs/file-utils";
import {doRegistration, isRegisterCodeValid, loadLogin, sendRegistrationRequest, storeLogin} from "@/libs/register";
import {goPath} from "@/libs/go-path";

export default function Page() {
    const [currStep, setCurrStep] = useState<number>(0);
    const [registerCode, setRegisterCode] = useState<string>("");
    const [selectedType, setSelectedType] = useState<AsymmCryptoType>(AsymmCryptoType.EC_C25519);
    const [currKeypair, setCurrKeypair] = useState<AsymmFullKeyPair | null>(null);
    const [currHandle, setCurrHandle] = useState<string | null>(null);
    const [exported, setExported] = useState<boolean>(false);

    const [checkKeypair, setCheckKeypair] = useState<AsymmFullKeyPair | null>(null);
    const [checkHandle, setCheckHandle] = useState<string | null>(null);

    const [storeLocal, setStoreLocal] = useState<boolean>(false);

    const [errorText, setErrorText] = useState<string | null>(null);

    useEffect(() => {
        if (currStep == 0 && currKeypair == null)
            generateNewKeypair().then();
    })

    async function generateNewKeypair() {
        const keypair = await generateAsymmKeypair(selectedType);
        const handle = await deriveHandleFromPublicSplitKey(keypair.pub);
        setCurrKeypair(keypair);
        setCurrHandle(handle);
        setExported(false);
    }

    async function _importKeypair(): Promise<AsymmFullKeyPair | null> {
        const importKeypairFile: File | null = await uploadData(false) as File;
        if (importKeypairFile == null)
            return null;

        const importKeypairObj = await readJsonFile<AsymmFullJsonKeypair>(importKeypairFile);
        if (importKeypairObj == null)
            return null;

        return parseFullKeypair(importKeypairObj);
    }

    async function importKeypair() {
        const keypair = await _importKeypair();
        const handle = keypair == null ? null : await deriveHandleFromPublicSplitKey(keypair.pub);

        if (currStep == 0) {
            setCurrKeypair(keypair);
            setCurrHandle(handle);
            if (currHandle != null)
                setExported(true);
        } else {
            setCheckKeypair(keypair);
            setCheckHandle(handle);

            if (handle == null || handle != currHandle)
                setErrorText("Uploaded Keypair does not match!");
            else
                setErrorText(null);
        }
    }

    async function exportKeypair() {
        if (currKeypair == null)
            return;

        const exportKeypair = serializeFullKeypair(currKeypair);
        downloadObjFile(exportKeypair, `keypair-${currHandle}.json`);
        setExported(true);
    }


    async function backToFirstStep() {
        setCurrStep(0);
    }

    async function checkFirstStep(): Promise<boolean> {
        setErrorText(null);
        if (registerCode == "") {
            setErrorText("Please enter a Register Code!");
            return false;
        }

        if (currKeypair == null || currHandle == null) {
            setErrorText("Please generate/import a Keypair first!");
            return false;
        }

        const codeValid: boolean = await checkRegisterCode(registerCode);
        if (!codeValid) {
            setErrorText("The Register Code is invalid!");
            return false;
        }

        if (!exported) {
            setErrorText("Please export the Keypair first!");
            return false;
        }

        return true;
    }

    async function continueFirstStep() {
        if (!(await checkFirstStep()))
            return;

        setCheckHandle(null);
        setCheckKeypair(null);
        setCurrStep(1);
    }

    async function toLastStep() {
        if (checkHandle == null || currKeypair == null) {
            setErrorText("You need to upload the Keypair!");
            return;
        }

        if (checkHandle != currHandle) {
            setErrorText("Uploaded Keypair does not match!");
            return;
        }

        const codeValid: boolean = await checkRegisterCode(registerCode);
        if (!codeValid) {
            setErrorText("The Register Code is invalid!");
            return;
        }

        const regErr = await doRegistration(registerCode, currKeypair);
        if (regErr != null) {
            setErrorText("Registration failed: " + regErr);
            return;
        }

        setCurrStep(2);
    }

    async function requestRegisterCode() {
        if (currKeypair == null)
            return;

        const message = prompt("Please enter a message to be sent to the server for requesting a Register Code.");
        if (message == null || message == "") {
            return;
        }
        const contact = prompt("Please enter a contact method for follow up on the request.");
        if (contact == null || contact == "") {
            return;
        }

        const req: RegistrationRequestDto = {
            message,
            contact
        };

        const err = await sendRegistrationRequest(req, currKeypair);
        if (err != null)
            alert("Error requesting Register Code: " + err);
        alert("Request sent! Please wait for a response from the server.");
    }

    async function checkRegisterCode(code: string): Promise<boolean> {
        let valid = true;
        if (code == "")
            valid = false;

        if (!(await isRegisterCodeValid(code)))
            valid = false;

        return valid;
    }

    async function updateStorageSetting(local: boolean) {
        await setStorageMode(local ? "LOCAL_STORAGE" : "SESSION_STORAGE");
        setStoreLocal(local);
        await saveKeypair(currKeypair);
    }

    async function setupPasswordBasedLogin() {
        if (currKeypair == null)
            return;

        // TODO: Improve UX
        const username = prompt("Please enter a username to be used for login.");
        if (username == null || username == "")
            return;
        const password = prompt("Please enter your password");
        if (password == null || password == "")
            return;

        const err = await storeLogin(username, password, currKeypair);
        if (err != null) {
            alert("Error Setting up Password based Login: " + err);
            return;
        }

        const res = await loadLogin(username, password);
        if (res == null)
            alert("Error loading login after setup!");
        else if (JSON.stringify(serializeFullKeypair(res)) != JSON.stringify(serializeFullKeypair(currKeypair)))
            alert("Error loading login after setup! Keypair does not match!");
        else
            alert("Password based login setup successfully!");
    }

    async function checkPasswordBasedLogin() {
        if (currKeypair == null)
            return;

        // TODO: Improve UX
        const username = prompt("Please your username.");
        if (username == null || username == "")
            return;
        const password = prompt("Please enter your password");
        if (password == null || password == "")
            return;

        const res = await loadLogin(username, password);
        if (res == null)
            alert("Incorrect Username or Password (or not setup)");
        else if (JSON.stringify(serializeFullKeypair(res)) != JSON.stringify(serializeFullKeypair(currKeypair)))
            alert("Error loading login after setup! Keypair does not match!");
        else
            alert("Password based login works!");
    }

    async function complete() {
        goPath("/user/home");
    }

    // TODO: Add better explanations
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

                        <br/><hr/><br/>
                        <label>Public Key: </label><br/>
                        <input type={"text"}     readOnly={true}  value={currKeypair?.pub.serialize() || ""}></input><br/><br/>
                        <label>Private Key: </label><br/>
                        <input type={"password"} readOnly={true}  value={currKeypair?.priv.serialize() || ""}></input><br/><br/>
                        <label>Handle: </label><br/>
                        <input type={"text"}     readOnly={true}  value={currHandle || ""}></input><br/>

                        <br/><hr/><br/>
                        <label>Enter Register Code:</label><br/>
                        <input type={"text"} placeholder={"Register Code"} value={registerCode} onChange={(e) => {
                            setRegisterCode(e.target.value);
                            checkRegisterCode(e.target.value).then();
                        }}></input><br/>

                        <br/><hr/><br/>
                        {errorText != null ? (<span className={styles.ErrorText}>{errorText}</span>) : null}<br/>

                        <button onClick={generateNewKeypair}>Generate Keypair</button><span> Type: </span>
                        <select value={selectedType.name} onChange={(e) => {
                            setSelectedType(AsymmCryptoType.fromValue(e.target.value));
                        }}>{AsymmCryptoType.TYPES.map((type) => <option key={type.name} value={type.name}>{type.name}</option>)}</select><br/>
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

                        <br/><hr/><br/>
                        <label>Public Key: </label><br/>
                        <input type={"text"}     disabled    value={checkKeypair?.pub.serialize() || ""}></input><br/>
                        <label>Private Key: </label><br/>
                        <input type={"password"} disabled    value={checkKeypair?.priv.serialize() || ""}></input><br/>
                        <label>Handle: </label><br/>
                        <input type={"text"} readOnly={true} value={checkHandle || ""}></input> = <input type={"text"}     readOnly={true} placeholder={"Handle"}      value={currHandle || ""}></input> ?<br/>

                        <br/><hr/><br/>
                        {errorText != null ? (<span className={styles.ErrorText}>{errorText}</span>) : null}<br/>

                        <button onClick={importKeypair}>Import Keypair</button><br/>

                        <br/>
                        <button disabled={checkHandle == null || currHandle != checkHandle} onClick={toLastStep}>Register</button><br/>
                        <button onClick={backToFirstStep}>Back</button><br/>
                    </div>
                </>) : (<></>)}

                {currStep === 2 ? (<>
                    <div>
                        <h3>Step 3: Preferences</h3>
                        <p>You are now registered! To finish up, please set up your preferences!</p>

                        <br/><hr/><br/>

                        <label>Your Handle: <b>{currHandle || ""}</b></label><br/>

                        <br/>
                        <label>Do you want to store your keypair in localStorage? </label>
                        <input type={"checkbox"} checked={storeLocal} onChange={(e) => {
                            updateStorageSetting(e.target.checked).then();
                        }}></input><br/>

                        <br/>
                        <label>Do you want to setup a password based login? </label>
                        <button onClick={setupPasswordBasedLogin}>Setup</button> <button onClick={checkPasswordBasedLogin}>Check</button><br/>

                        <br/><hr/><br/>
                        {errorText != null ? (<span className={styles.ErrorText}>{errorText}</span>) : null}<br/>

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
