package message.payloads.fragments.transaction;

import message.payloads.IPayload;
import util.ByteBufferFeed;
import util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record TransactionWitnessFragment(byte[] data) implements IPayload {

    public static TransactionWitnessFragment from(ByteBufferFeed feed) {
        int size = feed.pullVarInt();
        byte[] data = feed.pullBytes(size);

        return new TransactionWitnessFragment(data);
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(bufferSize())
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(Convert.toVarInt(data.length))
            .put(data)
            .flip();
    }

    public int bufferSize() {
        return Convert.toVarInt(data.length).length + data.length;
    }
}
