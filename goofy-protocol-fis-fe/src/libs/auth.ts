'use client';

import {MyUserInfoDto} from "@/libs/dtos";
import {getAuth} from "@/libs/req";
import {getKeypair, hasKeypair, saveKeypair} from "@/libs/auth-store";
import {deriveHandleFromPublicSplitKey} from "@/libs/crypto";
import {goPath} from "@/libs/go-path";

export async function isLoggedIn(): Promise<boolean> {
    return await hasKeypair();
}

export async function getMyHandle(): Promise<string | null> {
    if (!hasKeypair())
        return null;

    const keypair = await getKeypair();
    return await deriveHandleFromPublicSplitKey(keypair.pub);
}

export async function isUser(): Promise<boolean> {
    if (!(await isLoggedIn()))
        return false;

    try {
        const res: MyUserInfoDto = await getAuth("/fis-api/user/info");
        return res.authRole == "REGISTERED_USER" || res.authRole == "ADMIN";
    } catch {
        return false;
    }
}

export async function isAdmin(): Promise<boolean> {
    if (!(await isLoggedIn()))
        return false;

    try {
        const res: MyUserInfoDto = await getAuth("/fis-api/user/info");
        return res.authRole == "ADMIN";
    } catch {
        return false;
    }
}

export async function getUserInfo(throwError: boolean = false): Promise<MyUserInfoDto | null> {
    if (!(await isLoggedIn()))
        return null;

    try {
        const res: MyUserInfoDto = await getAuth("/fis-api/user/info");
        const derivedHandle = await getMyHandle();
        if (res.handle != derivedHandle) {
            alert(`Derived handle ${derivedHandle} does not match server handle ${res.handle}`);
            return null;
        }
        return res;
    } catch (e) {
        if (throwError)
            throw e;
        return null;
    }
}

export async function logout(): Promise<void> {
    await saveKeypair(null);
    goPath("/guest/login");
}