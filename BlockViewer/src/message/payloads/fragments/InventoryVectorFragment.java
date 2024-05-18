package message.payloads.fragments;

import message.payloads.IPayload;
import util.ByteBufferFeed;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public record InventoryVectorFragment(int type, byte[] hash) implements IPayload {
    public static int SIZE = 36;
    public enum InventoryType {ERROR, MSG_TX, MSG_BLOCK, MSG_FILTERED_BLOCK, MSG_CMPCT_BLOCK, MSG_WITNESS_TX, MSG_WITNESS_BLOCK, MSG_FILTERED_WITNESS_BLOCK}

    public InventoryType invType() {
        return InventoryType.values()[type()];
    }

    public static InventoryVectorFragment from(int type, byte[] hash) {
        return new InventoryVectorFragment(type, hash);
    }

    public static InventoryVectorFragment from(ByteBufferFeed feed) {
        return new InventoryVectorFragment(
            feed.pullInt32(),
            feed.pullBytes(32)
        );
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(bufferSize())
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(type)
            .put(hash)
            .flip();
    }

    public int bufferSize() {
        return 4 + 32;
    }

    @Override
    public String toString() {
        return " VersionPayload {" +
                "\n\t\t\ttype = " + InventoryType.values()[type] +
                ",\n\t\t\thash = " + Arrays.toString(hash) +
                "\n\t\t}";
    }
}