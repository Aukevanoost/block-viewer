package connection.workers;

import connection.monitoring.StreamMonitorFinder;
import message.BTCMessage;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class ConnectionCommandRetriever implements ConnectionWorker,Callable<BTCMessage> {
    private final StreamMonitorFinder monitor = new StreamMonitorFinder();
    private final ArrayBlockingQueue<BTCMessage> feed;
    private final String cmd;

    public ConnectionCommandRetriever(ArrayBlockingQueue<BTCMessage> buffer, String cmd) {
        this.feed = buffer;
        this.cmd = cmd;
    }

    public void fire() { this.monitor.cancel(); }

    @Override
    public BTCMessage call() throws TimeoutException {
        return this.monitor.find(this::checkForBTCMessage, 61);
    }

    private Optional<BTCMessage> checkForBTCMessage() {
        if(feed.isEmpty()) return Optional.empty();

        return Optional.ofNullable(feed.poll()).flatMap(msg -> {
            if(msg.command().equals(cmd)) {
                monitor.log("> PROCESS", msg.command(), msg.length());
                return Optional.of(msg);
            }
            return Optional.empty();
        });
    }
}

