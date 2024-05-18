package payloads.inv;

import payloads.IPayload;
import payloads.fragments.InventoryVectorFragment;
import util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public record InvPayload(List<InventoryVectorFragment> inventory) implements IPayload {
    public static InvPayloadBuilder builder() {
        return new InvPayloadBuilder();
    }

    public ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer
            .allocate(bufferSize())
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(Convert.toVarInt(inventory.size()).length);

        for (InventoryVectorFragment vector : inventory) {
            buffer.putInt(vector.type());
            buffer.put(vector.hash());
        }

        return buffer.flip();
    }
    public int bufferSize() {
        return Convert.toVarInt(inventory.size()).length + (inventory.size() * 36);
    }


}