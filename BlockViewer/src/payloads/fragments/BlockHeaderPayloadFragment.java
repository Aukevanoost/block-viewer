package payloads.fragments;

import payloads.IPayload;
import util.ByteBufferFeed;
import util.Convert;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public record BlockHeaderPayloadFragment(int version, byte[] prev_block, byte[] merkle_root, int timestamp, int bits, int nonce) implements IPayload {
    public static BlockHeaderPayloadFragment from(ByteBufferFeed feed) {
        return new BlockHeaderPayloadFragment(
            feed.pullInt32(),
            feed.pullBytes(32),
            feed.pullBytes(32),
            feed.pullInt32(),
            feed.pullInt32(),
            feed.pullInt32()
        );
    }
    public static BlockHeaderPayloadFragment from(int version, byte[] prev_block, byte[] merkle_root, int timestamp, int bits, int nonce) {
        return new BlockHeaderPayloadFragment(version,prev_block, merkle_root, timestamp, bits, nonce);
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(bufferSize())
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(version)
            .put(prev_block)
            .put(merkle_root)
            .putInt(timestamp)
            .putInt(bits)
            .putInt(nonce)
            .flip();
    }

    public int bufferSize() {
        return 4 + 32 + 32 + 4 + 4 + 4;
    }

    @Override
    public String toString() {
        return " BlockHeaderPayloadFragment {" +
            "\n\t\tversion = " + version +
            ",\n\t\tprev_block = " + Arrays.toString(prev_block) +
            ",\n\t\tmerkle_root = " + Arrays.toString(merkle_root) +
            ",\n\t\ttimestamp = " + timestamp +
            ",\n\t\tbits = " + bits +
            ",\n\t\tnonce = " + nonce +
        "\n\t}";
    }
}
