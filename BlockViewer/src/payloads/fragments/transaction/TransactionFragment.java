package payloads.fragments.transaction;

import payloads.IPayload;
import util.ByteBufferFeed;
import util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record TransactionFragment(int version, List<TransactionInFragment> txIn, List<TransactionOutFragment> txOut, List<TransactionWitnessFragment> txWitness, int lock_time) implements IPayload {
    public static TransactionFragment from(ByteBufferFeed feed) {
        int version = feed.pullInt32();
        boolean hasWitness = feed.pullOptionalBoolean();

        int sizeTxIn = feed.pullVarInt();
        var txIn = new ArrayList<TransactionInFragment>();
        for(int in = 0; in < sizeTxIn; in++) {
            txIn.add(TransactionInFragment.from(feed));
        }

        int sizeTxOut = feed.pullVarInt();
        var txOut = new ArrayList<TransactionOutFragment>();
        for(int out = 0; out < sizeTxOut; out++) {
            txOut.add(TransactionOutFragment.from(feed));
        }

        var txWitnesses = new ArrayList<TransactionWitnessFragment>();
        if(hasWitness) {
            int nWitnesses = feed.pullVarInt();
            for (int w = 0; w < nWitnesses; w++) {
                txWitnesses.add(TransactionWitnessFragment.from(feed));
            }
        }

        int lock_time = feed.pullInt32();

        return new TransactionFragment(
            version, txIn, txOut, txWitnesses, lock_time
        );
    }

    public ByteBuffer toBuffer() {
        var buffer = ByteBuffer.allocate(bufferSize())
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(version);

        if(txWitness.size() > 0) {
            buffer.put(new byte[] {0x00, 0x01});
        }

        if(txIn.size() > 0) {
            buffer.put(Convert.intToVarInt(txIn.size()));
            for (var t : txIn ) buffer.put(t.toBuffer());
        }

        if(txOut.size() > 0) {
            buffer.put(Convert.intToVarInt(txOut.size()));
            for (var t : txOut ) buffer.put(t.toBuffer());
        }

        if(txWitness.size() > 0) {
            buffer.put(Convert.intToVarInt(txWitness.size()));
            for (var t : txWitness ) buffer.put(t.toBuffer());
        }

        buffer.putInt(lock_time);
        return buffer.flip();
    }

    public int bufferSize() {
        int nVersion = Integer.BYTES; // size of version

        int nTxIn = payloadListBufferSize(txIn);

        int nTxOut = payloadListBufferSize(txOut);

        int witnessFlagSize = txWitness.size() > 0 ? 2 : 0;
        int nTxWitness = witnessFlagSize + payloadListBufferSize(txWitness);

        int nLockTime = Integer.BYTES; // size of lock_time

        return nVersion + nTxIn + nTxOut + nTxWitness + nLockTime;
    }



    private static int payloadListBufferSize(List<? extends IPayload> payloads) {
        var payloadSize = payloads.stream().reduce(
            0,
            (totalBufferSize, pl) -> totalBufferSize + pl.bufferSize(),
            Integer::sum
        );

        return Convert.intToVarInt(payloads.size()).length + payloadSize;
    }

}
