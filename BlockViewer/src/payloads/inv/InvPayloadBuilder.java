package payloads.inv;

import payloads.IPayloadBuilder;
import payloads.fragments.InventoryVectorFragment;
import util.ByteBufferFeed;
import java.util.ArrayList;
import java.util.List;

public class InvPayloadBuilder implements IPayloadBuilder {
    private List<InventoryVectorFragment> inventory = new ArrayList<>();


    public InvPayloadBuilder setInventory(List<InventoryVectorFragment> inventory) {
        this.inventory = inventory;
        return this;
    }
    public InvPayloadBuilder append(InventoryVectorFragment vector) {
        this.inventory.add(vector);
        return this;
    }

    public InvPayload from(ByteBufferFeed feed) {
        int length = feed.pullInt8();
        var inventory = new ArrayList<InventoryVectorFragment>();
        for(int i = 0; i < length; i++) {
            inventory.add(
                InventoryVectorFragment.from(feed)
            );
        }
        this.inventory = inventory;
        return build();
    }

    public InvPayload build() {
            return new InvPayload(inventory);
        }
}
