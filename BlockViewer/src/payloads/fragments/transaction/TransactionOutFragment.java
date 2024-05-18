package payloads.fragments.transaction;

import payloads.IPayload;
import util.ByteBufferFeed;
import util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public record TransactionOutFragment(long value, String script) implements IPayload {
    public static TransactionOutFragment from(ByteBufferFeed feed) {
        var value =  feed.pullLong();
        int size = feed.pullVarInt();
        String script = Convert.toHexString(feed.pullBytes(size));

        return new TransactionOutFragment(value, script);
    }

    public ByteBuffer toBuffer() {
        byte[] pk_key = Convert.hexToByteArray(script);
        byte[] scriptSizeInBytes = Convert.toVarInt(pk_key.length);
        return ByteBuffer.allocate(36 + scriptSizeInBytes.length + pk_key.length + Integer.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(value)
            .put(scriptSizeInBytes)
            .put(pk_key)
            .flip();
    }

    public int bufferSize() {
        return 8 + Convert.toVarInt(script.length()/2).length + script.length();
    }
}
