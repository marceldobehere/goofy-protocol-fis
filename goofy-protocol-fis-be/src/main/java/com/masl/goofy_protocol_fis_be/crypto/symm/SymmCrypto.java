package com.masl.goofy_protocol_fis_be.crypto.symm;

import java.util.List;

public interface SymmCrypto {
    List<SymmCryptoType> getTypes();

    byte[] encrypt(byte[] data, byte[] privSeed, SymmCryptoType type);
    byte[] decrypt(byte[] data, byte[] privSeed, SymmCryptoType type);
}
