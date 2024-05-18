package message.payloads.fragments.transaction;

import message.payloads.IPayload;
import util.ByteBufferFeed;
import util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record TransactionInFragment(byte[] prev_output, byte[] script, int sequence) implements IPayload {

    public static TransactionInFragment from(ByteBufferFeed feed) {
        var prev_output =  feed.pullBytes(36);
        int size = feed.pullVarInt();
        byte[] script = feed.pullBytes(size);
        int sequence = feed.pullInt32();

        return new TransactionInFragment(prev_output, script, sequence);
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(bufferSize())
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(prev_output)
            .put(Convert.toVarInt(script.length))
            .put(script)
            .putInt(sequence)
            .flip();
    }

    public int bufferSize() {
        return 36 + Convert.toVarInt(script.length).length + script.length + 4;
    }
}
