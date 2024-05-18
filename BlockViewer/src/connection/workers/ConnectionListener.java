package connection.workers;

import connection.monitoring.StreamMonitorExecutor;
import message.BTCMessage;
import util.ByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionListener implements ConnectionWorker, Runnable {
    private final StreamMonitorExecutor monitor = new StreamMonitorExecutor();

    private final ByteStream feed;
    private final LinkedBlockingQueue<BTCMessage> _messages = new LinkedBlockingQueue<>();
    public ConnectionListener(InputStream stream) {
        this.feed = ByteStream.of(stream);
    }

    public void fire() {
        this.monitor.fire();
    }

    @Override
    public void run()  {
        this.monitor.execute(this::processIncomingMessages);
    }

    private Boolean processIncomingMessages() {
        try {
            while(feed.bytesLeft() > 0) {
                var msg = BTCMessage.from(feed);
                monitor.log("> REC", msg.command(), msg.length());

                _messages.put(msg);
            }
        }catch(IOException|InterruptedException e) {
            System.out.println("Listener unfortunately passed away...");
            Thread.currentThread().interrupt();
            return true;
        }

        return false;
    }
}
