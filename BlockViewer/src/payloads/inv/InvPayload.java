package payloads.inv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class InvPayload {
    private final List<InventoryVector> inventory;

    public InvPayload(List<InventoryVector> inventory) {
        this.inventory = inventory;
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