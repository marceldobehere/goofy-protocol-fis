package com.masl.goofy_protocol_core.crypto.isolated.asymm;

import lombok.Getter;

@Getter
public enum AsymmCryptoType {
    RSA_2048((short)0x0100),
    RSA_3072((short)0x0101),
    RSA_4096((short)0x0102),

    EC_256((short)0x0200),
    EC_384((short)0x0201),

    MLKEMDSA_512_44((short)0x0300),
    MLKEMDSA_768_65((short)0x0301),
    MLKEMDSA_1024_87((short)0x0302);

    private final short value;
    AsymmCryptoType(short value) {
        this.value = value;
    }

    public static AsymmCryptoType fromValue(short value) {
        for (AsymmCryptoType t : values())
            if (t.value == value)
                return t;
        throw new IllegalArgumentException("Unknown AsymmCryptoType value: " + value);
    }
}