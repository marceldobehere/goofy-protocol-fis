import {MyUserInfoDto} from "@/libs/dtos";
import {getAuth} from "@/libs/req";
import {hasKeypair} from "@/libs/auth-store";

export async function isLoggedIn(): Promise<boolean> {
    return await hasKeypair();
}

export async function isUser(): Promise<boolean> {
    if (!(await isLoggedIn()))
        return false;

    try {
        const res: MyUserInfoDto = await getAuth("/api/user/info");
        return res.authRole == "REGISTERED_USER" || res.authRole == "ADMIN";
    } catch (_) {
        return false;
    }
}

export async function isAdmin(): Promise<boolean> {
    if (!(await isLoggedIn()))
        return false;

    try {
        const res: MyUserInfoDto = await getAuth("/api/user/info");
        return res.authRole == "ADMIN";
    } catch (_) {
        return false;
    }
}