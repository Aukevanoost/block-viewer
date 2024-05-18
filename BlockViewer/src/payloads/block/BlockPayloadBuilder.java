package payloads.block;

import util.ByteBufferFeed;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BlockPayloadBuilder {
    private int version;
    private byte[] prev_block;
    private byte[] merkle_root;
    private int timestamp;
    private int bits;
    private int nonce;
    private List<Object> transactions;


    public void from(ByteBufferFeed feed) {

        version = feed.pullInt32();
        prev_block = feed.pullBytes(32);
        merkle_root = feed.pullBytes(32);
        int nTransactions = feed.pullInt8();

        this.transactions = new ArrayList<Object>();
//        for(int i = 0; i < length; i++) {
//            int type = buffer.getInt();
//
//            byte[] hash = new byte[32];
//            buffer.get(hash);
//            inventory.add(new InventoryVector(type, hash));
//        }
//        return build();
    }

//    public BlockPayload build() {
//        return new BlockPayload(inventory);
//    }
}
