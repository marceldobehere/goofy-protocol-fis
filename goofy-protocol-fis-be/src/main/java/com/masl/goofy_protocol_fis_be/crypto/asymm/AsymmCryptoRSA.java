package com.masl.goofy_protocol_fis_be.crypto.asymm;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

public class AsymmCryptoRSA implements AsymmCrypto {
    @Override
    public List<AsymmCryptoType> getTypes() {
        return List.of(AsymmCryptoType.RSA_2048, AsymmCryptoType.RSA_3072, AsymmCryptoType.RSA_4096);
    }

    private static int keySize(AsymmCryptoType type) {
        return switch (type) {
            case RSA_2048 -> 2048;
            case RSA_3072 -> 3072;
            case RSA_4096 -> 4096;
            default -> throw new IllegalArgumentException("Invalid type");
        };
    }

    @Override
    public boolean checkPubKeyPair(AsymmPubKeyPair pubKeyPair, AsymmCryptoType type) {
        for (var pubKey : List.of(pubKeyPair.encKey(), pubKeyPair.sigKey()))
            try {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pubKey));
                Cipher encryptCipher = Cipher.getInstance("RSA");
                encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException e) {
                return false;
            }
        return true;
    }

    @Override
    public AsymmFullKeyPair generateKeypair(AsymmCryptoType type) {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        generator.initialize(keySize(type));
        KeyPair sigPair = generator.generateKeyPair();
        return AsymmFullKeyPair.fromParts(
                sigPair.getPublic().getEncoded(), sigPair.getPublic().getEncoded(),
                sigPair.getPrivate().getEncoded(), sigPair.getPrivate().getEncoded(),
                this, type
        );
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] pubEncKey, AsymmCryptoType type) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pubEncKey));
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return encryptCipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] privEncKey, AsymmCryptoType type) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privEncKey));
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return decryptCipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] sign(byte[] data, byte[] privSigKey, AsymmCryptoType type) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privSigKey));
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(data);
            return privateSignature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verify(byte[] data, byte[] sig, byte[] pubSigKey, AsymmCryptoType type) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pubSigKey));
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(data);
            return publicSignature.verify(sig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
