import {AsymmFullKeyPair} from "@/libs/crypto-types";

// This will be responsible for storing the keypair and loading it (either from SessionStorage or LocalStorage) and maybe secured with a password
// Other files can then check if there is a keypair loaded

let currKeypair: AsymmFullKeyPair | null;
let currServerBase: string;

let initDone = false;
export async function init() {
    if (initDone)
        return; // console.log("Keypair storage already initialized");
    initDone = true;
    console.log("Initializing keypair storage...");

    // TODO: Implement & call
    currServerBase = "http://localhost:8080";
    currKeypair = null;
}

export async function setStorageMode() {

}

export async function getBaseServerUrl(): Promise<string> {
    await init();

    return currServerBase;
}

export async function hasKeypair(): Promise<boolean> {
    await init();
    return currKeypair != null;
}

export async function getKeypair(): Promise<AsymmFullKeyPair> {
    await init();
    // TODO: Implement

    if (currKeypair == null)
        throw new Error("keypair is undefined");
    return currKeypair;
}

export async function saveKeypair(keypair: AsymmFullKeyPair) {
    await init();
    // TODO: Implement
    currKeypair = keypair;
}