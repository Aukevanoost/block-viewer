package payloads.fragments.transaction;

import payloads.IPayload;
import util.ByteBufferFeed;
import util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public record TransactionOutFragment(long value, byte[] script) implements IPayload {
    public static TransactionOutFragment from(ByteBufferFeed feed) {
        var value =  feed.pullLong();
        int size = feed.pullVarInt();
        byte[] script = feed.pullBytes(size);

        return new TransactionOutFragment(value, script);
    }

    public ByteBuffer toBuffer() {
        byte[] scriptSizeInBytes = Convert.intToVarInt(script.length);
        return ByteBuffer.allocate(36 + scriptSizeInBytes.length + script.length + Integer.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(value)
            .put(scriptSizeInBytes)
            .put(script)
            .flip();
    }

    public int bufferSize() {
        return 8 + Convert.intToVarInt(script.length).length + script.length;
    }
}
