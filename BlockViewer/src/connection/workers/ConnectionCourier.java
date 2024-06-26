package connection.workers;

import connection.monitoring.StreamMonitorExecutor;
import message.BTCMessage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionCourier implements ConnectionWorker, Runnable {
    private final StreamMonitorExecutor monitor = new StreamMonitorExecutor();
    private final DataOutputStream outputStream;
    public final LinkedBlockingQueue<BTCMessage> mailbox = new LinkedBlockingQueue<>();;

    public ConnectionCourier(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void fire() {
        monitor.cancel();
    }
    @Override
    public void run()  {
        this.monitor.execute(this::emptyMailbox, 52);
    }

    private Boolean emptyMailbox() {
        try {
            if(!mailbox.isEmpty()) {
                ArrayList<BTCMessage> mainBag = new ArrayList<>();
                mailbox.drainTo(mainBag);
                for (BTCMessage msg : mainBag) {
                    monitor.log("< SEN", msg.command(), msg.length());
                    outputStream.write(msg.feed().toArray());
                }
                outputStream.flush();
            }
        } catch(IOException e) {
            System.out.println("Mailman unfortunately passed away...");
            Thread.currentThread().interrupt();
            return true;
        }

        return false;
    }

}