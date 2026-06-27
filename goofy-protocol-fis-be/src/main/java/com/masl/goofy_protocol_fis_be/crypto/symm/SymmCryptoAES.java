package com.masl.goofy_protocol_fis_be.crypto.symm;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.List;

public class SymmCryptoAES implements SymmCrypto {
    @Override
    public List<SymmCryptoType> getTypes() {
        return List.of(SymmCryptoType.AES_GCM_128, SymmCryptoType.AES_GCM_192, SymmCryptoType.AES_GCM_256);
    }

    @Override
    public byte[] fromSecretString(String secretStr, SymmCryptoType type) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretStr.toCharArray(), "salt".getBytes(), 65536, keySize(type));
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return secret.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int keySize(SymmCryptoType type) {
        return switch (type) {
            case AES_GCM_128 -> 128;
            case AES_GCM_192 -> 192;
            case AES_GCM_256 -> 256;
            default -> throw new IllegalArgumentException("Invalid type");
        };
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] secret, SymmCryptoType type) {
        int keyBits = keySize(type);
        if (secret == null || secret.length * 8 != keyBits) {
            throw new IllegalArgumentException("Secret length must be " + (keyBits / 8) + " bytes for " + type);
        }

        try {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

            SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec);
            byte[] encryptedBytes = cipher.doFinal(data);

            byte[] combinedIvAndCipherText = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combinedIvAndCipherText, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combinedIvAndCipherText, iv.length, encryptedBytes.length);
            return combinedIvAndCipherText;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] secret, SymmCryptoType type) {
        int keyBits = keySize(type);
        if (secret == null || secret.length * 8 != keyBits) {
            throw new IllegalArgumentException("Secret length must be " + (keyBits / 8) + " bytes for " + type);
        }

        try {
            if (data == null || data.length < 12 + 16) { // IV (12) + minimum GCM tag (often 16)
                throw new IllegalArgumentException("Ciphertext too short");
            }

            byte[] iv = new byte[12];
            System.arraycopy(data, 0, iv, 0, 12);

            byte[] cipherTextAndTag = new byte[data.length - 12];
            System.arraycopy(data, 12, cipherTextAndTag, 0, cipherTextAndTag.length);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

            SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec);

            return cipher.doFinal(cipherTextAndTag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
