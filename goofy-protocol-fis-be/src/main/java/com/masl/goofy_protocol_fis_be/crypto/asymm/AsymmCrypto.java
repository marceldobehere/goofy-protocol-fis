package com.masl.goofy_protocol_fis_be.crypto.asymm;

import java.util.List;

public interface AsymmCrypto {
    List<AsymmCryptoType> getTypes();
    boolean checkPublicKey(byte[] pubKey);
    byte[] generatePublicKey(byte[] privSeed);

    byte[] encrypt(byte[] data, byte[] pubKey);
    byte[] decrypt(byte[] data, byte[] privSeed);
    byte[] sign(byte[] data, byte[] privSeed);
    boolean verify(byte[] sig, byte[] pubKey);
}

