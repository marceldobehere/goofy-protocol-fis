package com.masl.goofy_protocol_fis_be.crypto.symm;

import com.masl.goofy_protocol_fis_be.crypto.asymm.AsymmCryptoType;

import java.util.List;

public interface SymmCrypto {
    List<AsymmCryptoType> getTypes();

    byte[] encrypt(byte[] data, byte[] privSeed);
    byte[] decrypt(byte[] data, byte[] privSeed);
}
