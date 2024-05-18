# Bitcoin BlockViewer
Assignment 3 - Written in Java17, no external dependencies. 

To run the code you need and Java IDE like Intellij and JavaSE 17 installed. It's a simple console application so it should run out of the box. 

## 1. Architecture

<table>
    <tr>
        <th>Folder</th>
        <th>Description</th>
    </tr>
    <tr>
        <td><code>/connection</code></td>
        <td>Everything that is connected to the Socket. The connection is divided into 2 parts. Listeners and Couriers for the InputStream and the OutputStream respectively.  </td>
    </tr>
    <tr>
        <td><code>/message</code></td>
        <td>The DTO's for the message and different payload types. the <code>message.payloads.fragments</code> folder is for reusable sub-payloads like the Node (ip, port etc). </td>
    </tr>
    <tr>
        <td><code>/printer</code></td>
        <td>Dedicated to printers, that is, classes that print to the console. </td>
    </tr>
    <tr>
        <td><code>/util</code></td>
        <td>Custom byte streams and converters. Helpers.</td>
    </tr>
</table>

## 2. Connection
The communication is a bit wonky, however the idea is that the communication is handled by 3 workers. 
- A **courier** that sends packets to the Node, using the provided mailbox (concurrent queue).
- A **Listener** that queues up messages it receives from the Node and pongs every ping message it gets. The queue buffers up to 10 messages. 
- A **CommandRetriever** That can search in the buffer for or await a specific command. 

The MessageTracker is a Facade over the 'postService staff' (listener and courier) to encapsulate the communication and shutdown logic. 
```
    // The MessageTrackerFactory sets up the connection properly, initializing the courier and the listener. 
    public MessageTracker build() throws IOException {

        ExecutorService postService = Executors.newFixedThreadPool(3); // courier, listener and retriever

        var courier = new ConnectionCourier(new DataOutputStream(socket.getOutputStream()));
        postService.submit(courier);

        var listener = new ConnectionListener(socket.getInputStream(), courier.mailbox);
        postService.submit(listener);

        return new MessageTracker(listener, courier, postService);
    }
```

## 3. Payloads
For every payload used, there is a payload object. Every payload has its own conversion method to a bytearray/buffer. Since there is no native ByteStream like IntStream it's a bit of type freestyling but it works. The bufferSize is for allocation of the object to keep the memory footprint as small as possible. Complex payloads have their own builder but because of the high boilerplate level of Java some payloads have their builder baked in (from method)
```
public interface IPayload {
    ByteBuffer toBuffer();
    int bufferSize();
}
```

## 4. Util
I know that a Stream is supposed to be Immutable, but the ByteStream and ByteBufferFeed serve as Mutable streams that abstract away the real input stream and allow for a granular way of mapping byte arrays to objects and back. 

- The `ByteStream` is used in the `connection.workers.ConnectionListener` to map the `InputStream` to a `BTCMessage`. 
- The `ByteBufferFeed` is used by the payloads to extend the `ByteBuffer` behavior to strings and byte arrays.