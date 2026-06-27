package com.masl.goofy_protocol_fis_be.crypto.asymm;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public interface AsymmCrypto {
    List<AsymmCryptoType> getTypes();
    boolean checkPubKeyPair(AsymmPubKeyPair pubKeyPair, AsymmCryptoType type);
    AsymmFullKeyPair generateKeypair(AsymmCryptoType type);

    byte[] encrypt(byte[] data, byte[] pubEncKey, AsymmCryptoType type);
    byte[] decrypt(byte[] data, byte[] privEncKey, AsymmCryptoType type);

    byte[] sign(byte[] data, byte[] privSigKey, AsymmCryptoType type);
    boolean verify(byte[] data, byte[] sig, byte[] pubSigKey, AsymmCryptoType type);

    // PUB-[TYPE]-[SIG KEY]-[ENC KEY]-[ENC SIG]
    record AsymmPubKeyPair(byte[] sigKey, byte[] encKey, byte[] encSig, AsymmCryptoType type) {
        public static AsymmPubKeyPair parse(String value) {
            String[] parts = value.split("-");
            if (parts.length != 5)
                throw new IllegalArgumentException("Invalid split key format");
            if (!parts[0].equals("PUB"))
                throw new IllegalArgumentException("Invalid split key type");
            AsymmCryptoType type = AsymmCryptoType.valueOf(parts[1]);
            byte[] sigKey = Base64.getDecoder().decode(parts[2]);
            if (parts[3].equals(parts[4]) && parts[3].equals("X")) {
                return new AsymmPubKeyPair(sigKey, sigKey, null, type);
            } else {
                byte[] encKey = Base64.getDecoder().decode(parts[3]);
                byte[] encSig = Base64.getDecoder().decode(parts[4]);
                return new AsymmPubKeyPair(sigKey, encKey, encSig, type);
            }
        }

        public String serialize() {
            if (Arrays.equals(sigKey, encKey))
                return "PUB-" + type.toString() +
                        "-" + Base64.getEncoder().encodeToString(sigKey) +
                        "-" + "X" +
                        "-" + "X";
            return "PUB-" + type.toString() +
                    "-" + Base64.getEncoder().encodeToString(sigKey) +
                    "-" + Base64.getEncoder().encodeToString(encKey) +
                    "-" + Base64.getEncoder().encodeToString(encSig);
        }

        boolean isSigValid(AsymmCrypto crypto) {
            if (Arrays.equals(sigKey, encKey))
                return true;
            return crypto.verify(encKey, encSig, sigKey, type);
        }
    }

    // PRIV-[TYPE]-[SIG KEY]-[ENC KEY]
    record AsymmPrivKeyPair(byte[] sigKey, byte[] encKey, AsymmCryptoType type) {
        public static AsymmPrivKeyPair parse(String value) {
            String[] parts = value.split("-");
            if (parts.length != 4)
                throw new IllegalArgumentException("Invalid split key format");
            if (!parts[0].equals("PRIV"))
                throw new IllegalArgumentException("Invalid split key type");
            AsymmCryptoType type = AsymmCryptoType.valueOf(parts[1]);
            byte[] sigKey = Base64.getDecoder().decode(parts[2]);
            if (parts[3].equals("X")) {
                return new AsymmPrivKeyPair(sigKey, sigKey, type);
            } else {
                byte[] encKey = Base64.getDecoder().decode(parts[3]);
                return new AsymmPrivKeyPair(sigKey, encKey, type);
            }
        }

        public String serialize() {
            if (Arrays.equals(sigKey, encKey))
                return "PRIV-" + type.toString() +
                        "-" + Base64.getEncoder().encodeToString(sigKey) +
                        "-" + "X";

            return "PRIV-" + type.toString() +
                    "-" + Base64.getEncoder().encodeToString(sigKey) +
                    "-" + Base64.getEncoder().encodeToString(encKey);
        }
    }

    record AsymmFullKeyPair(AsymmPubKeyPair pub, AsymmPrivKeyPair priv) {
        static AsymmFullKeyPair fromParts(byte[] pubSigKey, byte[] pubEncKey, byte[] privSigKey, byte[] privEncKey, AsymmCrypto crypto, AsymmCryptoType type) {
            AsymmPrivKeyPair priv = new AsymmPrivKeyPair(privSigKey, privEncKey, type);
            byte[] encSig = Arrays.equals(pubEncKey, pubSigKey) ? null : crypto.sign(pubEncKey, privSigKey, type);
            AsymmPubKeyPair pub = new AsymmPubKeyPair(pubSigKey, pubEncKey, encSig, type);
            return new AsymmFullKeyPair(pub, priv);
        }
    }
}

