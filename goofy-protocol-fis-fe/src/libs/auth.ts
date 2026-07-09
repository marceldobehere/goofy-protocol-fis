import {MyUserInfoDto} from "@/libs/dtos";
import {getAuth} from "@/libs/req";
import {getKeypair, hasKeypair} from "@/libs/auth-store";
import {deriveHandleFromPublicSplitKey} from "@/libs/crypto";

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
        const res: MyUserInfoDto = await getAuth("/api/user/info");
        return res.authRole == "REGISTERED_USER" || res.authRole == "ADMIN";
    } catch {
        return false;
    }
}

export async function isAdmin(): Promise<boolean> {
    if (!(await isLoggedIn()))
        return false;

    try {
        const res: MyUserInfoDto = await getAuth("/api/user/info");
        return res.authRole == "ADMIN";
    } catch {
        return false;
    }
}