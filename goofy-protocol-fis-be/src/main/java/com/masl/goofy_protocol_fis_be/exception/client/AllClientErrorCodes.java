package com.masl.goofy_protocol_fis_be.exception.client;

// 1_XXX_YYY
public class AllClientErrorCodes {
    public static final int DEFAULT = 1_000_000;

    public static final int INVALID_SIGNATURE = 1_001_001;

    public static final int INVALID_REGISTER_CODE = 1_002_001;
    public static final int REGISTRATION_NOT_ALLOWED = 1_002_002;
    public static final int HANDLE_ALREADY_REGISTERED = 1_002_003;
    public static final int REGISTRATION_CODE_ALREADY_USED = 1_002_004;

    public static final int LOGIN_ENTRY_ALREADY_EXISTS = 1_003_001;
    public static final int LOGIN_ENTRY_NOT_FOUND = 1_003_002;
    public static final int LOGIN_ENTRY_INVALID = 1_003_003;

    public static final int INVALID_SIGNED_OBJECT = 1_004_001;
    public static final int INVALID_PUBLIC_KEY = 1_004_002;
    public static final int INVALID_PUBLIC_KEY_HANDLE_MAPPING = 1_004_003;

    public static final int IDENTITY_ENTRY_ALREADY_EXISTS = 1_005_001;
    public static final int IDENTITY_ENTRY_NOT_FOUND = 1_005_002;
    public static final int IDENTITY_ENTRY_INVALID = 1_005_003;
    public static final int IDENTITY_ENTRY_QUOTA_EXCEEDED = 1_005_004;

    public static final int SERVICE_ENTRY_QUOTA_EXCEEDED = 1_006_001;
    public static final int SERVICE_ENTRY_NOT_FOUND = 1_006_002;
    public static final int SERVICE_ENTRY_INVALID = 1_006_003;
}
