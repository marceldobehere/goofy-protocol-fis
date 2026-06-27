package com.masl.goofy_protocol_fis_be.crypto.symm;

import java.util.List;

public class SymmCryptoAES implements SymmCrypto {
    @Override
    public List<SymmCryptoType> getTypes() {
        return List.of();
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] secret, SymmCryptoType type) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] secret, SymmCryptoType type) {
        return new byte[0];
    }
}
