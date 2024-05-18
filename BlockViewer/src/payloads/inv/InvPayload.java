package payloads.inv;

import payloads.IPayload;
import payloads.ping.PingPayload;
import payloads.version.VersionPayloadBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public record InvPayload(List<InventoryVector> inventory) implements IPayload {
    public static InvPayloadBuilder builder() {
        return new InvPayloadBuilder();
    }

    public ByteBuffer toBuffer() {
        ByteBuffer buffer = ByteBuffer
            .allocate(4 + (inventory.size() * 36))
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(inventory.size());

        for (InventoryVector vector : inventory) {
            buffer.putInt(vector.type());
            buffer.put(vector.hash());
        }

        return buffer.flip();
    }


}