package com.masl.goofy_protocol_fis_be.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class SecretUtils {
    public static final String ENC_DELIMITER = ".";

    public static byte[] symmSecretFromSecret(String secret, String salt, int size) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, size);
        return factory.generateSecret(spec).getEncoded();
    }
}
