import connection.ConnectionCourier;
import connection.MessageTracker;
import message.BTCMessage;
import message.BTCNode;
import payloads.VersionPayload;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

public class Main {
    public static void main(String... args) {
    //    int[] ip = {51,195,28,51}; // 185.197.160.61
        String ip = "185.197.160.61";
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

                courier.mailbox.put(
                        BTCMessage.from(
                            "version",
                            VersionPayload.builder()
                                .setVersion(60002)
                                .setServices(services)
                                .setTimestamp(System.currentTimeMillis() / 1000)
                                .setReceiver(BTCNode.from(services, ip, port))
                                .setSender(BTCNode.from(services, "89.100.241.33", port))
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
                BTCMessage msg = tracker.await(trackedVersionMsg, 10, TimeUnit.SECONDS);
                var trackedPingMsg = tracker.track("ping");

                courier.mailbox.put( BTCMessage.empty("verack") );

                /*
                 * PONG CHECK
                 */
                BTCMessage pingMsg = tracker.await(trackedPingMsg, 1, TimeUnit.MINUTES);

                var trackedInvMessage = tracker.track("inv");
                var pongMsg = BTCMessage.from("pong", pingMsg.payload());

                courier.mailbox.put(
                        pongMsg
                );

                /*
                 * PONG CHECK
                 */
                BTCMessage invMessage = tracker.await(trackedInvMessage,20, TimeUnit.SECONDS);
                System.out.println("Size: " + invMessage.length());


                System.out.println("leaving now bye");

            } catch (InterruptedException e) {
                System.out.println("Interrupted: " + e.getMessage());
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
