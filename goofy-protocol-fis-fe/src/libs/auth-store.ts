import {AsymmFullKeyPair, AsymmPrivKeyPair, AsymmPubKeyPair} from "@/libs/crypto-types";
import {parsePrivateSplitKey, parsePublicSplitKey} from "@/libs/crypto";

// This will be responsible for storing the keypair and loading it (either from SessionStorage or LocalStorage) and maybe secured with a password
// Other files can then check if there is a keypair loaded

type StorageMode = "LOCAL_STORAGE" | "SESSION_STORAGE" | "NONE";

let currentStorageMode: StorageMode;
let currServerBase: string;
let currKeypair: AsymmFullKeyPair | null;

let initDone = false;
export async function init() {
    if (initDone)
        return;
    initDone = true;
    console.log("Initializing keypair storage...");

    // Load Storage Mode
    currentStorageMode = await _loadStorageMode();

    // Load Server Base
    currServerBase = await _loadServerBase(currentStorageMode, "http://localhost:8080");

    // Load Keypair
    currKeypair = await _loadKeypair(currentStorageMode);
}

// Public Functions

export async function setStorageMode(mode: StorageMode) {
    // Delete Old Data
    await _storeServerBase(null, currentStorageMode);
    await _storeKeypair(null, currentStorageMode);

    // Save New Data
    await _storeServerBase(currServerBase, mode);
    await _storeKeypair(currKeypair, mode);
    await _storeStorageMode(mode);
    currentStorageMode = mode;
}

export async function setBaseServerUrl(url: string) {
    await init();
    currServerBase = url;
    await _storeServerBase(url, currentStorageMode);
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
    if (currKeypair == null)
        throw new Error("keypair is undefined");
    return currKeypair;
}
export async function saveKeypair(keypair: AsymmFullKeyPair | null) {
    await init();
    currKeypair = keypair;
    await _storeKeypair(keypair, currentStorageMode);
}


// Internal Storage Mode
async function _loadStorageMode(): Promise<StorageMode> {
    let mode = localStorage.getItem("StorageMode") as StorageMode ?? "NONE";
    if (mode == "NONE")
        mode = "SESSION_STORAGE";
    localStorage.setItem("StorageMode", mode);
    return mode;
}
async function _storeStorageMode(mode: StorageMode) {
    localStorage.setItem("StorageMode", mode);
}

// Load / Store
async function _getStore(key: string, mode: StorageMode): Promise<string | null> {
    if (mode === "LOCAL_STORAGE")
        return localStorage.getItem(key);
    else if (mode === "SESSION_STORAGE")
        return sessionStorage.getItem(key);
    return null;
}
async function _setStore(key: string, value: string | null, mode: StorageMode) {
    if (value == null) {
        if (mode === "LOCAL_STORAGE")
            return localStorage.removeItem(key);
        else if (mode === "SESSION_STORAGE")
            return sessionStorage.removeItem(key);
    } else {
        if (mode === "LOCAL_STORAGE")
            return localStorage.setItem(key, value);
        else if (mode === "SESSION_STORAGE")
            return sessionStorage.setItem(key, value);
    }
}

// Internal Server Base
async function _loadServerBase(mode: StorageMode, defaultVal: string): Promise<string> {
    return await _getStore("ServerBase", mode) || defaultVal;
}
async function _storeServerBase(serverBase: string | null, mode: StorageMode) {
    await _setStore("ServerBase", serverBase, mode);
}

// Internal Keypair
async function _loadKeypair(mode: StorageMode): Promise<AsymmFullKeyPair | null> {
    const pubKeyStr = await _getStore("Login-PubKey", mode);
    const privKeyStr = await _getStore("Login-PrivKey", mode);
    if (pubKeyStr == null || privKeyStr == null)
        return null;

    const pubKey: AsymmPubKeyPair = parsePublicSplitKey(pubKeyStr);
    const privKey: AsymmPrivKeyPair = parsePrivateSplitKey(privKeyStr);
    return {
        pub: pubKey,
        priv: privKey
    };
}
async function _storeKeypair(keypair: AsymmFullKeyPair | null, mode: StorageMode) {
    if (keypair == null) {
        await _setStore("Login-PubKey", null, mode);
        await _setStore("Login-PrivKey", null, mode);
        return;
    }

    await _setStore("Login-PubKey", keypair.pub.serialize(), mode);
    await _setStore("Login-PrivKey", keypair.priv.serialize(), mode);
}


