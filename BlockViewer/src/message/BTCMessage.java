package message;

import util.ByteStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public record BTCMessage(byte[] magic, String command, int length, byte[] checksum, byte[] payload) {
    public static BTCMessage from(ByteStream stream) throws IOException {
        byte[] magic = stream.toArray(4);
        String cmd = new String(stream.buffer(12).array(), StandardCharsets.US_ASCII).trim();
        int length = stream.buffer(4).getInt();
        byte[] checksum = stream.toArray(4);
        byte[] payload = stream.toArray(length);
        return new BTCMessage(magic, cmd, length, checksum, payload);
    }

    public static BTCMessage from(String command, byte[] payload) {
        return new BTCMessage(
            new byte[] {(byte) 0xF9, (byte) 0xBE, (byte) 0xB4, (byte) 0xD9},
            command,
            payload.length,
            checksum(payload),
            payload
        );
    }

    public static BTCMessage empty(String command) {
        return BTCMessage.from(command, new byte[0]);
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer
            .allocate(4 + 12 + 4 + 4 + payload.length)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(magic)  // Magic value
            .put(Arrays.copyOf(command.getBytes(StandardCharsets.US_ASCII), 12))
            .putInt(payload.length)
            .put(checksum)
            .put(payload)
            .flip();
    }

    public byte[] toArray() {
        return toBuffer().array();
    }

    public static byte[] checksum(byte[] payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(digest.digest(payload));
            return Arrays.copyOf(hash, 4);  // First 4 bytes of the hash
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
