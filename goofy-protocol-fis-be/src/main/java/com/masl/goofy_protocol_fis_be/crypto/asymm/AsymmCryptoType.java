package com.masl.goofy_protocol_fis_be.crypto.asymm;

public enum AsymmCryptoType {
    RSA_2048,
    RSA_3072,
    RSA_4096,

    EC_256,
    EC_384,

    MLKEMDSA_512_44,
    MLKEMDSA_768_65,
    MLKEMDSA_1024_87,
}