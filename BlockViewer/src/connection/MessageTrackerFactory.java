package connection;

import connection.workers.ConnectionCourier;
import connection.workers.ConnectionListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageTrackerFactory {
    private final Socket socket;
    private MessageTrackerFactory(Socket socket) {
        this.socket = socket;
    }
    public static MessageTrackerFactory from(Socket socket) {
        return new MessageTrackerFactory(socket);
    }
    public MessageTracker build() throws IOException {

        ExecutorService executor = Executors.newFixedThreadPool(3); // courier, listener and retriever

        var courier = new ConnectionCourier(new DataOutputStream(socket.getOutputStream()));
        executor.submit(courier);

        var listener = new ConnectionListener(socket.getInputStream(), courier.mailbox);
        executor.submit(listener);

        return new MessageTracker(listener, courier, executor);
    }
}
