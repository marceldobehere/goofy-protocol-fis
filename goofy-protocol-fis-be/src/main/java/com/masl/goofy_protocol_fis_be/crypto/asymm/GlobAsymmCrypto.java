package com.masl.goofy_protocol_fis_be.crypto.asymm;

import java.util.List;

public class GlobAsymmCrypto {
    List<AsymmCrypto> cryptoList = List.of(new AsymmCryptoRSA(), new AsymmCryptoECC(), new AsymmCryptoMLKEMDSA());

    public List<AsymmCryptoType> getTypes() {
        return cryptoList.stream().map(AsymmCrypto::getTypes).flatMap(List::stream).toList();
    }

    public AsymmCrypto forType(AsymmCryptoType type) {
        return cryptoList.stream().filter(c -> c.getTypes().contains(type)).findFirst().orElse(null);
    }

    public boolean checkPublicSplitKey(String pubSplitKey) {
        AsymmCrypto.AsymmPubKeyPair parsed = AsymmCrypto.AsymmPubKeyPair.parse(pubSplitKey);
        AsymmCrypto crypto = forType(parsed.type());
        if (crypto == null)
            throw new IllegalArgumentException("Invalid type");
        if (!crypto.checkPubKeyPair(parsed, parsed.type()))
            return false;
        if (!parsed.isSigValid(crypto))
            return false;
        return true;
    }

    public AsymmCrypto.AsymmFullKeyPair generateKeypair(AsymmCryptoType type) {
        AsymmCrypto crypto = forType(type);
        if (crypto == null)
            throw new IllegalArgumentException("Invalid type");
        return crypto.generateKeypair(type);
    }

    public byte[] encrypt(byte[] data, String pubSplitKey) {
        AsymmCrypto.AsymmPubKeyPair parsed = AsymmCrypto.AsymmPubKeyPair.parse(pubSplitKey);
        AsymmCrypto crypto = forType(parsed.type());
        if (crypto == null)
            throw new IllegalArgumentException("Invalid type");
        if (!parsed.isSigValid(crypto))
            throw new IllegalArgumentException("Public Key Sig is not valid");

        return crypto.encrypt(data, parsed.encKey(), parsed.type());
    }

    public byte[] decrypt(byte[] data, String privSplitKey) {
        AsymmCrypto.AsymmPrivKeyPair parsed = AsymmCrypto.AsymmPrivKeyPair.parse(privSplitKey);
        AsymmCrypto crypto = forType(parsed.type());
        if (crypto == null)
            throw new IllegalArgumentException("Invalid type");

        return crypto.decrypt(data, parsed.encKey(), parsed.type());
    }

    public byte[] sign(byte[] data, String privSplitKey) {
        AsymmCrypto.AsymmPrivKeyPair parsed = AsymmCrypto.AsymmPrivKeyPair.parse(privSplitKey);
        AsymmCrypto crypto = forType(parsed.type());
        if (crypto == null)
            throw new IllegalArgumentException("Invalid type");

        return crypto.sign(data, parsed.sigKey(), parsed.type());
    }

    public boolean verify(byte[] data, byte[] sig, String pubSplitKey) {
        AsymmCrypto.AsymmPubKeyPair parsed = AsymmCrypto.AsymmPubKeyPair.parse(pubSplitKey);
        AsymmCrypto crypto = forType(parsed.type());
        if (crypto == null)
            throw new IllegalArgumentException("Invalid type");

        return crypto.verify(data, sig, parsed.sigKey(), parsed.type());
    }


    public static final AsymmCryptoType DEFAULT_TYPE = AsymmCryptoType.ECC_256;
    public AsymmCrypto.AsymmFullKeyPair generateKeypair() {
        return generateKeypair(DEFAULT_TYPE);
    }
}
