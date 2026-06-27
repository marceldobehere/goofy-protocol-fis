package com.masl.goofy_protocol_fis_be.crypto.symm;

import com.masl.goofy_protocol_fis_be.crypto.asymm.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class GlobSymmCrypto {
    List<SymmCrypto> cryptoList = List.of(new SymmCryptoAES());

    public List<SymmCryptoType> getTypes() {
        return cryptoList.stream().map(SymmCrypto::getTypes).flatMap(List::stream).toList();
    }

    public SymmCrypto forType(SymmCryptoType type) {
        return cryptoList.stream().filter(c -> c.getTypes().contains(type)).findFirst().orElse(null);
    }

    public record ParsedEncData(byte[] data, SymmCryptoType type) {
        public static ParsedEncData parse(String value) {
            String[] parts = value.split("-");
            if (parts.length != 2)
                throw new IllegalArgumentException("Invalid data format");
            SymmCryptoType type = SymmCryptoType.valueOf(parts[0]);
            byte[] data = Base64.getDecoder().decode(parts[1]);
            return new ParsedEncData(data, type);
        }

        public String serialize() {
            return type.toString() + "-" + Base64.getEncoder().encodeToString(data);
        }
    }


    public String encrypt(byte[] data, String secret, SymmCryptoType type) {
        SymmCrypto crypto = forType(type);
        if (crypto == null || data == null || secret == null)
            throw new IllegalArgumentException("Invalid type, data or secret");
        byte[] res = crypto.encrypt(data, crypto.fromSecretString(secret, type), type);
        return new ParsedEncData(res, type).serialize();
    }

    public byte[] decrypt(String data, String secret) {
        ParsedEncData parsed = ParsedEncData.parse(data);
        SymmCrypto crypto = forType(parsed.type());
        if (crypto == null || secret == null)
            throw new IllegalArgumentException("Invalid type or secret");
        return crypto.decrypt(parsed.data, crypto.fromSecretString(secret, parsed.type()), parsed.type());
    }

    public byte[] encryptRaw(byte[] data, String secret, SymmCryptoType type) {
        SymmCrypto crypto = forType(type);
        if (crypto == null || data == null || secret == null)
            throw new IllegalArgumentException("Invalid type, data or secret");
        byte[] res = crypto.encrypt(data, crypto.fromSecretString(secret, type), type);

        ByteBuffer buf = ByteBuffer.allocate(2 + res.length);
        buf.putShort((short) type.ordinal());
        buf.put(res);
        return buf.array();
    }

    public byte[] decryptRaw(byte[] data, String secret) {
        if (data == null || secret == null || data.length < 2)
            throw new IllegalArgumentException("Invalid type or secret");

        ByteBuffer buf = ByteBuffer.wrap(data);
        int ordinal = buf.getShort() & 0xFFFF; // keep unsigned
        SymmCryptoType type = SymmCryptoType.values()[ordinal];

        SymmCrypto crypto = forType(type);
        if (crypto == null)
            throw new IllegalArgumentException("Invalid type or secret");

        byte[] enc = new byte[buf.remaining()];
        buf.get(enc);

        return crypto.decrypt(enc, crypto.fromSecretString(secret, type), type);
    }


    public static final SymmCryptoType DEFAULT_TYPE = SymmCryptoType.AES_GCM_192;

    public String encrypt(byte[] data, String secret) {
        return encrypt(data, secret, DEFAULT_TYPE);
    }
}
