package connection;

import connection.workers.ConnectionCommandRetriever;
import connection.workers.ConnectionCourier;
import connection.workers.ConnectionListener;
import message.BTCMessage;

import java.io.InputStream;
import java.util.concurrent.*;

public record MessageTracker(
        ConnectionListener listener,
        ConnectionCourier courier,
        ExecutorService postService
) {


    public Future<BTCMessage> track(String cmd) {
        return postService.submit(
            new ConnectionCommandRetriever(listener.buffer, cmd)
        );
    }

    public BTCMessage await(Future<BTCMessage> trackedMsg, int timeout, TimeUnit timeUnit) throws InterruptedException {
        try {
            return trackedMsg.get(timeout, timeUnit);
        } catch (TimeoutException e) {
            trackedMsg.cancel(true);
            throw new InterruptedException("Timeout expired, task is cancelled");
        } catch (ExecutionException e) {
            throw new InterruptedException("Execution failed");
        }
    }

    public BTCMessage await(String cmd, int timeout, TimeUnit timeUnit) throws InterruptedException {
        return await(track(cmd), timeout, timeUnit);
    }

    public void deliver(BTCMessage msg) throws InterruptedException  {
        this.courier.mailbox.put(msg);
    }

    public void shutdown() {
        this.courier.fire();
        this.listener.fire();

        this.postService.shutdown();
    }
}
