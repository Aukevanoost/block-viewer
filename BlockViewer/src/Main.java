import connection.ConnectionCourier;
import connection.MessageTracker;
import message.BTCMessage;
import payloads.fragments.NodePayloadFragment;
import payloads.version.VersionPayload;
import util.ByteBufferFeed;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
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
                                .setReceiver(NodePayloadFragment.from(services, ip, port))
                                .setSender(NodePayloadFragment.from(services, "89.100.241.33", port))
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
                                .setReceiver(NodePayloadFragment.from(services, ip, port))
                                .setSender(NodePayloadFragment.from(services, "89.100.241.33", port))
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
//                var payloadFeed = ByteBufferFeed.from(ByteBuffer.wrap(versionMsg.payload()).order(ByteOrder.LITTLE_ENDIAN));
//                VersionPayload versionPL = VersionPayload.builder().from(payloadFeed);
//                System.out.println(versionPL.toString());

                var trackedPingMsg = tracker.track("ping");

                courier.mailbox.put( BTCMessage.empty("verack") );

                /*
                 * PONG CHECK
                 */
                BTCMessage pingMsg = tracker.await(trackedPingMsg, 1, TimeUnit.MINUTES);

                var trackedInvMessage = tracker.track("inv");
                courier.mailbox.put(BTCMessage.from("pong", pingMsg.payload()));

                /*
                 * INVENTORY CHECK
                 */
                BTCMessage invMessage = tracker.await(trackedInvMessage,10, TimeUnit.MINUTES);
//                VersionPayload invMessagePL = VersionPayload.builder().from(
//                    ByteBufferFeed.from(versionMsg.payload())
//                );
//                System.out.println(invMessagePL.toString());

                System.out.println("=== leaving now bye ===");

            }
//            catch (InterruptedException e) {
//                System.out.println("Interrupted: " + e.getMessage());
//            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exiting postService.");
            }

            courier.fire();
            postService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        var req = new BitcoinVersionRequest();
//        req.connect("185.197.160.61");
    }
}
