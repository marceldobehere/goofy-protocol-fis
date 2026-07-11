export interface FisExceptionDto {
    errorCode: number;
    message: string;
    details: Map<string, object> | null;
}

export class RequestError extends Error {
    httpCode: number;
    message: string;

    constructor(httpCode: number, error: string) {
        super();
        this.httpCode = httpCode;
        this.message = error;
    }

    toString(): string {
        return `${this.httpCode} -> ${this.message}`;
    }
}

export class RequestFisError extends Error {
    httpCode: number;
    errorCode: number;
    message: string;
    details: Map<string, object> | null;

    constructor(httpCode: number, error: FisExceptionDto) {
        super();
        this.httpCode = httpCode;
        this.errorCode = error.errorCode;
        this.message = error.message;
        this.details = error.details;
    }

    toString(): string {
        return `${this.httpCode} ${this.errorCode} -> ${this.message} (${JSON.stringify(this.details)})`;
    }
}

// 1_XXX_YYY
export class AllClientErrorCodes {
    static readonly DEFAULT: number = 1_000_000;
    static readonly INVALID_SIGNATURE: number = 1_001_001;
    static readonly INVALID_REGISTER_CODE: number = 1_002_001;
    static readonly REGISTRATION_NOT_ALLOWED: number = 1_002_002;
    static readonly HANDLE_ALREADY_REGISTERED: number = 1_002_003;
    static readonly REGISTRATION_CODE_ALREADY_USED: number = 1_002_004;
}

// 2_XXX_YYY
export class AllServerErrorCodes {
    static readonly DEFAULT: number = 2_000_000;
    static readonly PUBLIC_KEY_LOOKUP_FAILED: number = 2_001_001;
}



export interface GeneralInfoDto {
    frontendUrl: string;
    url: string;
    name: string;
    description: string;
    version: string;
    pubKey: string;
    handle: string;
    supportedAsymmCryptoTypes: string[];
    supportedSymmCryptoTypes: string[];
}

export interface RegisterStatusDto {
    registrationsAllowed: boolean;
    checkMethod: string;
}

export type AuthRole = "OUTSIDE_ENTITY" | "REGISTERED_USER" | "ADMIN";

export interface MyUserInfoDto {
    handle: string;
    handleDomain: string;
    pubKey: string;
    authRole: AuthRole;
    isRestricted: boolean;
}

export interface RegistrationRequestDto {
    message: string;
    contact: string;
    optEmail?: string;
}

export interface IdentityStorageEntryDto {
    handle: string;
    name: string;
    pubSplitKey: string;
    encKeypairEntry: string;
    encKeypairEntrySignature: string;
}

export interface MyIdentityEntryQuotasDto {
    maxEntryCount: number;
    currentEntryCount: number;
}

export interface ServiceEntryDto {
    name: string;
    uuid: string;
    usedService?: string;
}

export interface MyServiceEntryQuotasDto {
    maxServiceEntryCount: number;
    currentServiceEntryCount: number;
}