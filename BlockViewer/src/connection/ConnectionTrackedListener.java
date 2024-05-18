package connection;

import message.BTCMessage;
import util.ByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class ConnectionTrackedListener implements Callable<BTCMessage> {
    private final ByteStream feed;
    private final String cmd;
    private volatile boolean fired = false;
    public ConnectionTrackedListener(InputStream stream, String cmd) {
        this.feed = ByteStream.of(stream);
        this.cmd = cmd;
    }

    public void fire() {
        fired = true;
    }
    @Override
    public BTCMessage call()  {
        BTCMessage trackedMessage = null;

        try {
            while(!fired && !Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {
                while(feed.bytesLeft() > 0) {
                    var incomingMessage = BTCMessage.from(feed);
                    if(incomingMessage.command().equals(cmd)) {
                        trackedMessage = incomingMessage;
                        this.fire();
                    }
                }
            }
        }catch(IOException e) {
            System.out.println("TrackedListener unfortunately passed away...");
            this.fired = true;
        }

        return trackedMessage;
    }
}

