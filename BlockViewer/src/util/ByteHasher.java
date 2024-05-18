package util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public record ByteHasher(byte[] raw) {

    public static ByteHasher from(byte[] raw) {
        return new ByteHasher(raw);
    }

    public static ByteHasher from(ByteBuffer raw) {
        return new ByteHasher(raw.array());
    }

    public byte[] hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashI = digest.digest(raw);
            return digest.digest(hashI);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
