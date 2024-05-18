package printer;

import message.payloads.block.BlockPayload;
import message.payloads.fragments.transaction.TransactionOutFragment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BlockPrinter {
    private final BlockPayload payload;
    private BlockPrinter(BlockPayload payload) {
        this.payload = payload;
    }

    public static BlockPrinter from(BlockPayload payload) {
        return new BlockPrinter(payload);
    }

    public void print() {
        System.out.println("+----------------------------------------------------------------");
        System.out.format("| BLOCK DETAILS (%d bytes)\n", payload.bufferSize());
        System.out.format("| Created: %s \n", BlockPrinter.formatTimestamp(payload.header().timestamp()));
        System.out.format(
                "| Nonce: %s (difficulty: %d bits) \n",
                payload.header().nonce(),
                payload.header().difficultyInBits()
        );
        System.out.println("+----------------------------------------------------------------");
        long totalValue = 0;
        for(var tx : payload.transactions()) {
            long txValue = tx.txOut().stream().mapToLong(TransactionOutFragment::value).sum() ;
            System.out.format(
                    "| -- fragment: \t %11.6f btc \t-\t %3d tx IN  \t-\t %3d tx OUT \t-\t %2d Witnesses\n",
                    txValue / 100000000.0,
                    tx.txIn().size(),
                    tx.txOut().size(),
                    tx.txWitness().size()
            );
            totalValue += txValue;
        }
        System.out.format("| -- TOTAL VALUE (%f btc)\n", totalValue / 100000000.0);
        System.out.println("+----------------------------------------------------------------");

    }

    private static String formatTimestamp(long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' HH:mm");
        return dateTime.format(formatter);
    }
}
