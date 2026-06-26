package com.masl.goofy_protocol_fis_be.crypto.asymm;

import java.util.List;

public class AsymmCryptoRSA implements AsymmCrypto {
    @Override
    public List<AsymmCryptoType> getTypes() {
        return List.of();
    }

    @Override
    public boolean checkPublicKey(byte[] pubKey) {
        return false;
    }

    @Override
    public byte[] generatePublicKey(byte[] privSeed) {
        return new byte[0];
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] pubKey) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] privSeed) {
        return new byte[0];
    }

    @Override
    public byte[] sign(byte[] data, byte[] privSeed) {
        return new byte[0];
    }

    @Override
    public boolean verify(byte[] sig, byte[] pubKey) {
        return false;
    }
}
