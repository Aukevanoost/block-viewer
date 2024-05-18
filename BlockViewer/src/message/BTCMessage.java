package message;

import util.ByteBufferFeed;
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
        var headerFeed = ByteBufferFeed.from(stream.buffer(4 + 12 + 4 + 4));

        byte[] magic = headerFeed.pullBytes(4);// stream.buffer(4).array();
        String cmd = headerFeed.pullString(12);
        int length = headerFeed.pullInt32();
        byte[] checksum = headerFeed.pullBytes(4);

        return new BTCMessage(
            magic,
            cmd,
            length,
            checksum,
            stream.buffer(length).array()
        );
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

    public ByteBufferFeed feed() {
        return ByteBufferFeed.from(
            ByteBuffer
                .allocate(4 + 12 + 4 + 4 + payload.length)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(magic)  // Magic value
                .put(Arrays.copyOf(command.getBytes(StandardCharsets.US_ASCII), 12))
                .putInt(payload.length)
                .put(checksum)
                .put(payload)
                .flip()
        );
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
}
