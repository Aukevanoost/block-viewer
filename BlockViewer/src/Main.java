import connection.MessageTrackerFactory;
import connection.workers.ConnectionCourier;
import connection.workers.ConnectionListener;
import message.BTCMessage;
import message.payloads.block.BlockPayload;
import message.payloads.fragments.NodeFragment;
import message.payloads.getdata.GetDataPayload;
import message.payloads.inv.InvPayload;
import message.payloads.fragments.InventoryVectorFragment;
import message.payloads.version.VersionPayload;
import printer.BlockPrinter;
import util.ByteBufferFeed;
import util.ByteHasher;
import util.Convert;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    public static void main(String... args) {
        // Trying not to annoy a single server
        String ip = getRandomIPAddress();

        short port = 8333;
        long services = 1;

        try (Socket socket = new Socket(ip, port)) {
            System.out.format(
                "=====| Connection established with Node %s at %s |=====\n",
                ip,
                new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss").format(new java.util.Date())
            );

            var tracker = MessageTrackerFactory
                .from(socket)
                .build();

            try {
                /*
                 * === VERSION CONNECTION
                 */
                tracker.deliver(
                    BTCMessage.from(
                        "version",
                        VersionPayload.builder()
                            .setVersion(70015)
                            .setServices(services)
                            .setTimestamp(System.currentTimeMillis() / 1000)
                            .setReceiver(NodeFragment.from(services, ip, port))
                            .setSender(NodeFragment.from(services, "89.100.241.33", port))
                            .setNonce(69)
                            .setUserAgent("/Satoshi:0.15.2/")
                            .setStartHeight(0)
                            .setRelay(true)
                            .build().toBuffer().array()
                    )
                );


                /*
                 * === VERRACK
                 */
                tracker.await("version",10, TimeUnit.SECONDS);
                tracker.deliver( BTCMessage.empty("verack") );

                /*
                 * === WAIT FOR INV MESSAGE WITH BLOCK
                 */
                var foundBlock = false;

                InventoryVectorFragment blockInvVector = null;
                while (!foundBlock) {
                    BTCMessage invMessage = tracker.await("inv",30, TimeUnit.MINUTES);

                    InvPayload invMessagePL = InvPayload.builder().from(
                        ByteBufferFeed.from(invMessage.payload()) // Check if block msg
                    );

                    for (InventoryVectorFragment v : invMessagePL.inventory()) {
                        if(v.invType() == InventoryVectorFragment.InventoryType.MSG_BLOCK) {
                            foundBlock = true;
                            blockInvVector = v;
                            break;
                        }
                    }
                }

                /*
                 * === FETCH BLOCK BASED ON INV VECTOR
                 */
                tracker.deliver(
                    BTCMessage.from(
                        "getdata",
                        GetDataPayload.from(blockInvVector)
                            .toBuffer()
                            .array()
                    )
                );

                BTCMessage blockMsg = tracker.await("block",1, TimeUnit.MINUTES);

                var blockPayload = BlockPayload.from(
                    ByteBufferFeed.from(blockMsg.payload()) // Check if block msg
                );


                /*
                 * === PRINT BLOCK
                 */
                BlockPrinter.from(blockPayload).print();

                var hashedBlock = ByteHasher
                    .from(blockPayload.header().toBuffer())
                    .hash();

                System.out.format( "Block header    : %s\n",  Convert.toHexString(hashedBlock));
                System.out.format( "Inventory vector: %s\n",  Convert.toHexString(blockInvVector.hash()));
            }
            catch (InterruptedException e) {
                System.out.println("Interrupted: " + e.getMessage());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Exiting postService.");

            tracker.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRandomIPAddress() {
        List<String> ipAddresses = List.of("34.92.95.72",
                "140.238.220.99",   "182.69.118.149",   "38.242.206.56",
                "47.202.79.149",    "3.238.8.21",       "3.71.96.148",
                "15.237.37.136",    "3.248.223.135",    "37.60.247.190",
                "34.93.21.82",      "5.9.87.228",       "5.225.145.111",
                "57.135.69.226",    "5.164.29.36",      "18.197.172.46"
        );

        return ipAddresses.get(
            (new Random()).nextInt(ipAddresses.size())
        );
    }
}
