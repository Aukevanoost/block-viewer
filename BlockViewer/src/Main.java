import connection.workers.ConnectionCourier;
import connection.MessageTracker;
import message.BTCMessage;
import payloads.block.BlockPayload;
import payloads.fragments.BlockHeaderFragment;
import payloads.fragments.NodeFragment;
import payloads.getdata.GetDataPayload;
import payloads.inv.InvPayload;
import payloads.fragments.InventoryVectorFragment;
import payloads.version.VersionPayload;
import printer.BlockPrinter;
import util.ByteBufferFeed;
import util.Convert;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    public static void main(String... args) {
        // String ip = "185.197.160.61";
        // String ip = "51.195.28.51";

        // Trying not to annoy a single server
        String ip = getRandomIPAddress();


        short port = 8333;
        long services = 1;

        ExecutorService postService = Executors.newFixedThreadPool(2);

        try (Socket socket = new Socket(ip, port)) {
            System.out.format("=====| Connection established with Node %s at %s |=====\n", ip, new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss").format(new java.util.Date()));
            var tracker = new MessageTracker(socket.getInputStream(), postService);
            var courier = new ConnectionCourier(new DataOutputStream(socket.getOutputStream()));
            postService.submit(courier);

            try {
                /*
                 * VERSION CONNECTION
                 */
                var trackedVersionMsg= tracker.track("version");

                courier.mailbox.put(
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
                 * VERACK CONNECTION
                 */
                BTCMessage versionMsg = tracker.await(trackedVersionMsg, 10, TimeUnit.SECONDS);
                BTCMessage headerMsg = tracker.await(trackedVersionMsg, 10, TimeUnit.SECONDS);
                var trackedPingMsg = tracker.track("ping");

                courier.mailbox.put( BTCMessage.empty("verack") );

                /*
                 * PONG CHECK
                 */
                BTCMessage pingMsg = tracker.await(trackedPingMsg, 1, TimeUnit.MINUTES);
                courier.mailbox.put(BTCMessage.from("pong", pingMsg.payload()));

                /*
                 * INVENTORY CHECK
                 */
                var foundBlock = false;

                InventoryVectorFragment blockInvVector = null;
                while (!foundBlock) {
                    var trackedInvMessage = tracker.track("inv");

                    BTCMessage invMessage = tracker.await(trackedInvMessage,30, TimeUnit.MINUTES);

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
                 * FETCH BLOCK DATA
                 */
                var trackedBlockMsg = tracker.track("block");

                courier.mailbox.put(
                    BTCMessage.from(
                        "getdata",
                        GetDataPayload.from(blockInvVector)
                            .toBuffer()
                            .array()
                    )
                );
                BTCMessage blockMsg = tracker.await(trackedBlockMsg, 1, TimeUnit.MINUTES);
                var blockPayload = BlockPayload.from(
                    ByteBufferFeed.from(blockMsg.payload()) // Check if block msg
                );

                (new BlockPrinter(blockPayload)).print();

                var hashedBlock = getHash(blockPayload.header());
                System.out.format( "B:%s\n",  Convert.toHexString(hashedBlock));
                System.out.format( "I:%s\n",  Convert.toHexString(blockInvVector.hash()));

                System.out.println("=== leaving now bye ===");

            }
            catch (InterruptedException e) {
                System.out.println("Interrupted: " + e.getMessage());
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exiting postService.");
            }

            courier.fire();
            postService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getHash(BlockHeaderFragment blockHeader) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(blockHeader.toBuffer().array());
            return digest.digest(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
