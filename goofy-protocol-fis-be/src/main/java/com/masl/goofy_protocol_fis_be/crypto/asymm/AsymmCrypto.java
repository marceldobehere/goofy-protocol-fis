package com.masl.goofy_protocol_fis_be.crypto.asymm;

import java.util.List;

public interface AsymmCrypto {
    List<AsymmCryptoType> getTypes();
    boolean checkPublicKey(byte[] pubKey, AsymmCryptoType type);
    byte[] generatePublicKey(byte[] privSeed, AsymmCryptoType type);

    byte[] encrypt(byte[] data, byte[] pubKey, AsymmCryptoType type);
    byte[] decrypt(byte[] data, byte[] privSeed, AsymmCryptoType type);
    byte[] sign(byte[] data, byte[] privSeed, AsymmCryptoType type);
    boolean verify(byte[] sig, byte[] pubKey, AsymmCryptoType type);
}

