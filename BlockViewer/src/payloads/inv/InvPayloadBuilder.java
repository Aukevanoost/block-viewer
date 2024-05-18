package payloads.inv;

import payloads.IPayloadBuilder;
import util.ByteBufferFeed;
import java.util.ArrayList;
import java.util.List;

public class InvPayloadBuilder implements IPayloadBuilder {
    private List<InventoryVector> inventory = new ArrayList<>();


    public InvPayloadBuilder setInventory(List<InventoryVector> inventory) {
        this.inventory = inventory;
        return this;
    }
    public InvPayloadBuilder append(InventoryVector vector) {
        this.inventory.add(vector);
        return this;
    }

    public InvPayload from(ByteBufferFeed feed) {
        int length = feed.pullInt8();
        var inventory = new ArrayList<InventoryVector>();
        for(int i = 0; i < length; i++) {
            inventory.add(new InventoryVector(
                feed.pullInt32(),
                feed.pullBytes(32)
            ));
        }
        this.inventory = inventory;
        return build();
    }

    public InvPayload build() {
            return new InvPayload(inventory);
        }
}
