package connection.workers;

import connection.monitoring.StreamMonitorExecutor;
import message.BTCMessage;
import util.ByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionListener implements ConnectionWorker, Runnable {
    private static final int MAX_BUFFERED_ITEMS = 10;
    private final StreamMonitorExecutor monitor = new StreamMonitorExecutor();

    private final ByteStream feed;
    public final ArrayBlockingQueue<BTCMessage> buffer = new ArrayBlockingQueue<>(MAX_BUFFERED_ITEMS+1);
    private final LinkedBlockingQueue<BTCMessage> courier_mailbox;
    public ConnectionListener(
        InputStream stream,
        LinkedBlockingQueue<BTCMessage> courier_mailbox
    ) {
        this.feed = ByteStream.of(stream);
        this.courier_mailbox = courier_mailbox;
    }

    public void fire() {
        this.monitor.cancel();
    }

    @Override
    public void run()  {
        this.monitor.execute(this::processIncomingMessages,10);
    }

    private Boolean processIncomingMessages() {
        try {
            while(feed.bytesLeft() > 0) {
                var msg = BTCMessage.from(feed);
                monitor.log("> REC", msg.command(), msg.length());

                if (msg.command().equals("ping")) {
                    courier_mailbox.put(BTCMessage.from("pong", msg.payload()));
                } else {
                    buffer.put(msg);
                    if(buffer.size() > MAX_BUFFERED_ITEMS) buffer.poll();
                }
            }
            if(feed.bytesLeft() < 0) {
                throw new IOException("Stream closed");
            }
        }catch(IOException|InterruptedException e) {
            System.out.println("Listener unfortunately passed away...");
            Thread.currentThread().interrupt();
            return true;
        }

        return false;
    }
}
