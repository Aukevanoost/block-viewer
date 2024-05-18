package payloads.block;

import payloads.IPayload;
import payloads.fragments.BlockHeaderPayloadFragment;
import payloads.fragments.transaction.TransactionFragment;
import util.ByteBufferFeed;
import util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public record BlockPayload(BlockHeaderPayloadFragment header, List<TransactionFragment> transactions) implements IPayload {
    public static BlockPayload from(ByteBufferFeed feed) {
        var header = BlockHeaderPayloadFragment.from(feed);

        int nTransactions = feed.pullVarInt();
        var transactions = new ArrayList<TransactionFragment>();
        for(int tx = 0; tx < nTransactions; tx++) {
            transactions.add(TransactionFragment.from(feed));
        }
        return new BlockPayload(header, transactions);
    }

    public ByteBuffer toBuffer() {
        var buffer = ByteBuffer.allocate(bufferSize())
                .order(ByteOrder.LITTLE_ENDIAN);

        if(transactions.size() > 0) {
            buffer.put(Convert.intToVarInt(transactions.size()));
            for (var t : transactions ) buffer.put(t.toBuffer());
        }
        return buffer.flip();
    }
    public int bufferSize() {
        return header.bufferSize() + Convert.payloadListToBufferSize(transactions);
    }

}