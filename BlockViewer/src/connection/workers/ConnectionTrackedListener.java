package connection.workers;

import connection.monitoring.StreamMonitorFinder;
import message.BTCMessage;
import util.ByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class ConnectionTrackedListener implements ConnectionWorker,Callable<BTCMessage> {
    private final StreamMonitorFinder monitor = new StreamMonitorFinder();
    private final ByteStream feed;
    private final String cmd;

    public ConnectionTrackedListener(InputStream stream, String cmd) {
        this.feed = ByteStream.of(stream);
        this.cmd = cmd;
    }

    public void fire() { this.monitor.fire(); }

    @Override
    public BTCMessage call() throws TimeoutException {
        return this.monitor.find(this::checkForBTCMessage);
    }

    private Optional<BTCMessage> checkForBTCMessage() {
        try {
            while(feed.bytesLeft() > 0) {
                var msg = BTCMessage.from(feed);
                if(msg.command().equals(cmd)) {
                    monitor.log("> FOUND", msg.command(), msg.length());
                    return Optional.of(msg);
                }
                monitor.log("> REC", msg.command(), msg.length());
            }
            if(feed.bytesLeft() < 0) {
                throw new IOException("Stream closed");
            }
        }catch(IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }
}

