package com.masl.goofy_protocol_fis_be.entity;

public class FieldSize {
    public static final int HANDLE_LEN = 200;
    public static final int PUB_KEY_LEN = 20_000;
    public static final int PRIV_KEY_LEN = 30_000;
    public static final int FULL_KEY_LEN = (int)((PUB_KEY_LEN + PRIV_KEY_LEN) * 1.33); // Extra Overhead for Base64 Encoding
    public static final int SIGNATURE_LEN = 10_000;
    public static final int GENERIC_CODE_LEN = 128;
    public static final int SHA256_LEN = 200;

    public static final int TITLE_LEN = 100;
    public static final int SHORT_TEXT_LEN = 300;
    public static final int NORMAL_TEXT_LEN = 1000;
    public static final int LONG_TEXT_LEN = 10_000;
}
