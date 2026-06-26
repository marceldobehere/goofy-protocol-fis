package com.masl.goofy_protocol_fis_be.crypto.asymm;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class GlobAsymmCrypto {
    List<AsymmCrypto> cryptoList = List.of(new AsymmCryptoRSA(), new AsymmCryptoECC(), new AsymmCryptoMLKEMDSA());

    public List<AsymmCryptoType> getTypes() {
        return cryptoList.stream().map(AsymmCrypto::getTypes).flatMap(List::stream).toList();
    }

    public AsymmCrypto forType(AsymmCryptoType type) {
        return cryptoList.stream().filter(c -> c.getTypes().contains(type)).findFirst().orElse(null);
    }

    public record ParsedPubKey(byte[] pubKey, AsymmCryptoType type) {
        static ParsedPubKey parse(String value) {
            String[] parts = value.split("#");
            if (parts.length != 2)
                throw new IllegalArgumentException("Invalid public key format");
            AsymmCryptoType type = AsymmCryptoType.valueOf(parts[0]);
            byte[] pubKey = Base64.getDecoder().decode(parts[1]);
            return new ParsedPubKey(pubKey, type);
        }

        String serialize() {
            return type.toString() + "#" + Base64.getEncoder().encodeToString(pubKey);
        }
    }


    public boolean checkPublicKey(String pubKey) {
        try {
            ParsedPubKey parsed = ParsedPubKey.parse(pubKey);
            AsymmCrypto crypto = forType(parsed.type());
            if (crypto == null) {
                return false;
            }
            return crypto.checkPublicKey(parsed.pubKey());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String generatePublicKey(String privSeed, AsymmCryptoType type) {
        AsymmCrypto crypto = forType(type);
        if (crypto == null || privSeed == null)
            throw new IllegalArgumentException("Invalid type or privSeed");
        byte[] pubKey = crypto.generatePublicKey(privSeed.getBytes(StandardCharsets.UTF_8));
        return new ParsedPubKey(pubKey, type).serialize();
    }


    public byte[] encrypt(byte[] data, String pubKey) {
        ParsedPubKey parsed = ParsedPubKey.parse(pubKey);
        AsymmCrypto crypto = forType(parsed.type());
        if (crypto == null || data == null)
            throw new IllegalArgumentException("Invalid type or data");
        return crypto.encrypt(data, parsed.pubKey());
    }

    public byte[] decrypt(byte[] data, String privSeed, AsymmCryptoType type) {
        AsymmCrypto crypto = forType(type);
        if (crypto == null || data == null || privSeed == null)
            throw new IllegalArgumentException("Invalid type, data or privSeed");
        return crypto.decrypt(data, privSeed.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] sign(byte[] data, String privSeed, AsymmCryptoType type) {
        AsymmCrypto crypto = forType(type);
        if (crypto == null || data == null || privSeed == null)
            throw new IllegalArgumentException("Invalid type, data or privSeed");
        return crypto.sign(data, privSeed.getBytes(StandardCharsets.UTF_8));
    }

    public boolean verify(byte[] sig, String pubKey) {
        ParsedPubKey parsed = ParsedPubKey.parse(pubKey);
        AsymmCrypto crypto = forType(parsed.type());
        if (crypto == null || sig == null)
            throw new IllegalArgumentException("Invalid type or sig");
        return crypto.verify(sig, parsed.pubKey());
    }

    public static final AsymmCryptoType DEFAULT_TYPE = AsymmCryptoType.ECC_256;

    public String generatePublicKey(String privSeed) {
        return generatePublicKey(privSeed, DEFAULT_TYPE);
    }

    public byte[] decrypt(byte[] data, String privSeed) {
        return decrypt(data, privSeed, DEFAULT_TYPE);
    }

    public byte[] sign(byte[] data, String privSeed) {
        return sign(data, privSeed, DEFAULT_TYPE);
    }
}
