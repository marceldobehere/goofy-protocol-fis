package com.masl.goofy_protocol_fis_be.crypto.asymm;

import java.util.List;

public class AsymmCryptoMLKEMDSA implements AsymmCrypto {

    @Override
    public List<AsymmCryptoType> getTypes() {
        return List.of();
    }

    @Override
    public boolean checkPubKeyPair(AsymmPubKeyPair pubKeyPair, AsymmCryptoType type) {
        return false;
    }

    @Override
    public AsymmFullKeyPair generateKeypair(AsymmCryptoType type) {
        return null;
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] pubEncKey, AsymmCryptoType type) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] privEncKey, AsymmCryptoType type) {
        return new byte[0];
    }

    @Override
    public byte[] sign(byte[] data, byte[] privSigKey, AsymmCryptoType type) {
        return new byte[0];
    }

    @Override
    public boolean verify(byte[] data, byte[] sig, byte[] pubSigKey, AsymmCryptoType type) {
        return false;
    }
}
