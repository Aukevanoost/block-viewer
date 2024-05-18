import connection.workers.ConnectionCourier;
import connection.MessageTracker;
import message.BTCMessage;
import payloads.block.BlockPayload;
import payloads.fragments.NodeFragment;
import payloads.getdata.GetDataPayload;
import payloads.inv.InvPayload;
import payloads.fragments.InventoryVectorFragment;
import payloads.version.VersionPayload;
import util.ByteBufferFeed;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;

public class Main {
    public static void main(String... args) {
    //    int[] ip = {51,195,28,51}; // 185.197.160.61
        String ip = "185.197.160.61";
        //String ip = "51.195.28.51";
        short port = 8333;
        long services = 1;

        ExecutorService postService = Executors.newFixedThreadPool(2);

        try (Socket socket = new Socket(ip, port)) {
            System.out.format("Connection established: %s\n",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()));
            var tracker = new MessageTracker(socket.getInputStream(), postService);
            var courier = new ConnectionCourier(new DataOutputStream(socket.getOutputStream()));
            postService.submit(courier);

            try {
                /*
                 * VERSION CONNECTION
                 */
                var trackedVersionMsg= tracker.track("version");

                /// ### TEST ###



                // System.out.println(Arrays.toString(test));
                var versionMsgOut = BTCMessage.from(
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
                );
//                var payloadFeedOut = ByteBufferFeed.from(ByteBuffer.wrap(versionMsgOut.payload()).order(ByteOrder.LITTLE_ENDIAN));
//                VersionPayload versionPLOut = VersionPayload.builder().from(payloadFeedOut);
//                System.out.println(versionPLOut.toString());

                courier.mailbox.put(
                        BTCMessage.from(
                            "version",
                            VersionPayload.builder()
                                .setVersion(60002)
                                .setServices(services)
                                .setTimestamp(System.currentTimeMillis() / 1000)
                                .setReceiver(NodeFragment.from(services, ip, port))
                                .setSender(NodeFragment.from(services, "89.100.241.33", port))
                                .setNonce(69)
                                .setUserAgent("/Satoshi:0.7.2/")
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
//                var payloadFeed = ByteBufferFeed.from(ByteBuffer.wrap(versionMsg.payload()).order(ByteOrder.LITTLE_ENDIAN));
//                VersionPayload versionPL = VersionPayload.builder().from(payloadFeed);
//                System.out.println(versionPL.toString());

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
                            System.out.println("FOUND A BLOCK!!!");
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
                var dataPayload = BlockPayload.from(
                    ByteBufferFeed.from(blockMsg.payload()) // Check if block msg
                );

                System.out.format("Received block! (%d bytes)\n", dataPayload.bufferSize());

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
}
