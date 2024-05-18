package connection;

import message.BTCMessage;
import util.ByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionListener implements Runnable {
    private final ByteStream feed;
    private final LinkedBlockingQueue<BTCMessage> _messages = new LinkedBlockingQueue<>();
    private volatile boolean fired = false;
    public ConnectionListener(InputStream stream) {
        this.feed = ByteStream.of(stream);
    }

    public void fire() {
        fired = true;
    }
    @Override
    public void run()  {
        try {
            while(!fired && !Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {
                while(feed.bytesLeft() > 0) {
                    var res = BTCMessage.from(feed);
                    System.out.println("> received: " + res.command());
                    _messages.put(res);
                }
            }
        }catch(IOException|InterruptedException e) {
            System.out.println("Listener unfortunately passed away...");
            this.fired = true;
            Thread.currentThread().interrupt();
        }
    }
}
