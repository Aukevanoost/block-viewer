package connection;

import message.BTCMessage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionCourier implements Runnable {
    private final DataOutputStream outputStream;
    public final LinkedBlockingQueue<BTCMessage> mailbox = new LinkedBlockingQueue<>();
    private volatile boolean fired = false;
    public ConnectionCourier(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void fire() {
        fired = true;
    }
    @Override
    public void run()  {
        try {
            while(!fired && !Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {
                if(!mailbox.isEmpty()) {
                    ArrayList<BTCMessage> mailbuffer = new ArrayList<>();
                    mailbox.drainTo(mailbuffer);
                    for(BTCMessage msg : mailbuffer) {
                        System.out.format("< SEN: %s (%d bytes)\n", msg.command(), msg.length());
                        outputStream.write(msg.feed().toArray());
                    }
                    outputStream.flush();
                }
            }
        }catch(IOException e) {
            System.out.println("Mailman unfortunately passed away...");
            this.fired = true;
            Thread.currentThread().interrupt();
        }
    }

}