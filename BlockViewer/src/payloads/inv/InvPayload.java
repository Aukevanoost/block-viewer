package payloads.inv;

import payloads.IPayload;
import payloads.fragments.InventoryVectorFragment;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public record InvPayload(List<InventoryVectorFragment> inventory) implements IPayload {
    public static InvPayloadBuilder builder() {
        return new InvPayloadBuilder();
    }

    public ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer
            .allocate(4 + (inventory.size() * 36))
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(inventory.size());

        for (InventoryVectorFragment vector : inventory) {
            buffer.putInt(vector.type());
            buffer.put(vector.hash());
        }

        return buffer.flip();
    }
    public int bufferSize() {
        return 4 + (inventory.size() * 36);
    }


}