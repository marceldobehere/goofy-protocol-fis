import {AsymmFullKeyPair, HttpMethod} from "@/libs/crypto-types";
import {createSignedRequest, getHeadersFromSignedRequestWithHandle, getHeadersFromSignedRequestWithPubkey} from "@/libs/crypto";
import {AllServerErrorCodes, FisExceptionDto, RequestError, RequestFisError} from "@/libs/dtos";
import {getBaseServerUrl, getKeypair} from "@/libs/auth";

export async function _internalDoReq<T>(_path: string, method: HttpMethod, body: object | null, keypair: AsymmFullKeyPair | null = null, extraHeaders: Map<string, string> = new Map(), rawResponse: boolean = false, sendHandle: boolean = true): Promise<T | Response> {
    const headers: Map<string, string> = new Map(extraHeaders);
    const isBodyStr = body != null && typeof body === "string";
    if (!isBodyStr && body != null)
        headers.set("Content-Type", "application/json");

    // Fix Path
    let path = _path;
    if (path.startsWith("/"))
        path = await getBaseServerUrl() + path;
    const basePath = new URL(path).pathname;

    // Generate Signed Request and add Headers
    if (keypair != null) {
        const bodyVal = (body == null) ? null : (isBodyStr ? body : JSON.stringify(body));
        const req = await createSignedRequest(keypair, method, basePath, bodyVal);
        const reqHeaders = sendHandle ? getHeadersFromSignedRequestWithHandle(req) : getHeadersFromSignedRequestWithPubkey(req);
        for (const [key, value] of reqHeaders.entries())
            headers.set(key, value);
    }

    // Prepare Request Options
    const reqOptions: RequestInit = {
        method,
        headers: headers.entries().toArray(),
    };

    // Add Body if needed
    if (body != null)
        reqOptions.body = isBodyStr ? body : JSON.stringify(body);

    // console.log(`> Sending ${method} Request to ${path} ${keypair == null ? 'without auth' : 'with auth'} and options: `, reqOptions, body);

    // Execute fetch
    let res = await fetch(path, reqOptions);
    // console.log(`< Received Response from ${path} with status ${res.status} and ok=${res.ok}`, res);

    // Check for PublicKeyLookupFailed Error (if sendHandle is enabled) and retry if needed
    if (keypair != null && sendHandle && !res.ok) {
        const resBodyStr = await res.text();
        try {
            const resBody = JSON.parse(resBodyStr);
            if (resBody satisfies FisExceptionDto && (resBody as FisExceptionDto).errorCode == AllServerErrorCodes.PUBLIC_KEY_LOOKUP_FAILED ) {
                return await _internalDoReq<T>(_path, method, body, keypair, extraHeaders, rawResponse, false);
            }
        } catch (_) {}

        // Reconstruct Response and hope its fine
        res = new Response(resBodyStr, {status: res.status, statusText: res.statusText, headers: res.headers});
    }

    // Send raw Response
    if (rawResponse)
        return res;

    // If Response is not ok, throw Error in shape of RequestFisError (Fis Exceptions) or RequestError (General Exceptions)
    if (!res.ok) {
        const errorStr = await res.text();
        let errorObj: FisExceptionDto;
        try {
            errorObj = JSON.parse(errorStr) as FisExceptionDto;
        } catch (_) {
            throw new RequestError(res.status, errorStr);
        }
        throw new RequestFisError(res.status, errorObj);
    }

    // Convert to String or Object
    const resStr = await res.text();
    try {
        return JSON.parse(resStr) as T;
    } catch (_) {
        return resStr as T;
    }
}


export async function getNoAuth<T>(path: string): Promise<T> {
    return await _internalDoReq<T>(path, "GET", null) as T;
}

export async function getRawNoAuth(path: string): Promise<Response> {
    return await _internalDoReq<Response>(path, "GET", null, null, new Map(), true) as Response;
}

export async function getAuth<T>(path: string): Promise<T> {
    return await _internalDoReq<T>(path, "GET", null, await getKeypair()) as T;
}

export async function postNoAuth<T>(path: string, body: object) {
    return await _internalDoReq<T>(path, "POST", body) as T;
}

export async function postRawNoAuth(path: string, body: object): Promise<Response> {
    return await _internalDoReq<Response>(path, "POST", body, null, new Map(), true) as Response;
}

export async function postAuth<T>(path: string, body: object) {
    return await _internalDoReq<T>(path, "POST", body) as T;
}

// TODO: Add more when needed