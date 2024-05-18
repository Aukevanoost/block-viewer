package payloads.fragments;

import payloads.IPayload;
import util.ByteBufferFeed;
import util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public record BlockHeaderFragment(int version, byte[] prev_block, byte[] merkle_root, int timestamp, int difficultyInBits, String nonce) implements IPayload {
    public static BlockHeaderFragment from(ByteBufferFeed feed) {
        return new BlockHeaderFragment(
            feed.pullInt32(),
            feed.pullBytes(32),
            feed.pullBytes(32),
            feed.pullInt32(),
            feed.pullInt32(),
            Convert.toHexString(feed.pullBytes(4))
        );
    }
    public static BlockHeaderFragment from(int version, byte[] prev_block, byte[] merkle_root, int timestamp, int difficultyInBits, String nonce) {
        return new BlockHeaderFragment(version,prev_block, merkle_root, timestamp, difficultyInBits, nonce);
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(bufferSize())
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(version)
            .put(prev_block)
            .put(merkle_root)
            .putInt(timestamp)
            .putInt(difficultyInBits)
            .put(Convert.hexToByteArray(nonce))
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
            ",\n\t\tbits = " + difficultyInBits +
            ",\n\t\tnonce = " + nonce +
        "\n\t}";
    }
}
