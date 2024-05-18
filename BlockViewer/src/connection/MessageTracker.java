package connection;

import message.BTCMessage;

import java.io.InputStream;
import java.util.concurrent.*;

public record MessageTracker(InputStream feed, ExecutorService executor) {


    public Future<BTCMessage> track(String cmd) {
        return executor.submit(
            new ConnectionTrackedListener(feed, cmd)
        );
    }

    public BTCMessage await(Future<BTCMessage> trackedMsg, int timeout, TimeUnit timeUnit) throws InterruptedException {
        try {
            var msg = trackedMsg.get(timeout, timeUnit);
            System.out.println("> Arrived!: " + msg.command());
            return msg;
        } catch (TimeoutException e) {
            trackedMsg.cancel(true);
            throw new InterruptedException("Timeout expired, task is cancelled");
        } catch (ExecutionException e) {
            throw new InterruptedException("Execution failed");
        }
    }
}
