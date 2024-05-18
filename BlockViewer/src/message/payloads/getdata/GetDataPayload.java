package message.payloads.getdata;

import message.payloads.IPayload;
import message.payloads.fragments.InventoryVectorFragment;
import util.ByteBufferFeed;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public record GetDataPayload(List<InventoryVectorFragment> vectors) implements IPayload {
    public static GetDataPayload from(InventoryVectorFragment inventoryVector) {
        return new GetDataPayload(List.of(inventoryVector));
    }

    public static GetDataPayload from(ByteBufferFeed feed) {
        byte size = feed.pullByte();

        var vectors = new ArrayList<InventoryVectorFragment>();
        for(int i = 0; i < size; i++) {
            vectors.add(InventoryVectorFragment.from(feed));
        }

        return new GetDataPayload(vectors);
    }

    public ByteBuffer toBuffer() {
        var buffer = ByteBuffer
            .allocate(bufferSize())
            .order(ByteOrder.LITTLE_ENDIAN)
            .put((byte) vectors.size());

        for(var vector : vectors) {
            buffer.put(vector.toBuffer());
        }

        return buffer.flip();
    }

    public int bufferSize() {
        return Byte.BYTES + (InventoryVectorFragment.SIZE * vectors.size());
    }
}