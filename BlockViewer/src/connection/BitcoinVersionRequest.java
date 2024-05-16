package connection;

import util.BytesConverter;

import java.io.*;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BitcoinVersionRequest {
    public void connect(int[] ip) {
        int nodePort = 8333;  // Default Bitcoin network port
        String address = Arrays.stream(ip)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("."));

        try (Socket socket = new Socket(address, nodePort)) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            byte[] versionPayload = constructVersionPayload(ip);
            byte[] message = constructMessage(versionPayload);

            outputStream.write(message);
            outputStream.flush();

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            readInput(inputStream);

            System.out.println("bye");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] constructMessage(byte[] payload) {
        ByteBuffer buffer = ByteBuffer
                .allocate(1024)
                .order(ByteOrder.LITTLE_ENDIAN);

        buffer
                .putInt(0xD9B4BEF9)  // Magic value
                .put(Arrays.copyOf("version".getBytes(StandardCharsets.US_ASCII), 12))
                .putInt(payload.length)
                .put(calculateChecksum(payload))
                .flip();

        byte[] header = new byte[buffer.limit()];
        buffer.get(header);

        byte[] message = new byte[header.length + payload.length];
        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(payload, 0, message, header.length, payload.length);
        return message;
    }

    public byte[] calculateChecksum(byte[] payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload);
            hash = digest.digest(hash);
            return Arrays.copyOf(hash, 4);  // First 4 bytes of the hash
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] constructVersionPayload(int[] addr) {
        // Version message fields
        int version = 60002;
        long services = 1;
        long timestamp = System.currentTimeMillis() / 1000;

        short port = 8333;
        long nonce = 69;
        byte[] user_agent = "/Satoshi:0.7.2/".getBytes(StandardCharsets.UTF_8);
        int start_height = 0;
        boolean relay = true;

        // Create a byte buffer to store the payload
        ByteBuffer buffer = ByteBuffer
                .allocate(1024)
                .order(ByteOrder.LITTLE_ENDIAN);

//        System.out.println(user_agent.length);
        // Write the fields to the buffer
        buffer
            .putInt(version)        // 4
            .putLong(services)      // 8
            .putLong(timestamp)     // 8
            .put(this.getNetworkAddr(services, addr, port))                             // 26 -> 46
            .put(this.getNetworkAddr(services, new int[] {89,100,241,33}, port))         // 26
            .putLong(nonce)         // 8
            .put((byte) user_agent.length) //  1
            .put(user_agent)        // 15
            .putInt(start_height)   // 4        -> 54
            .put((byte) (relay ? 1 : 0))// 1  -> 1
            .flip();

        // Get the payload bytes
        byte[] payload = new byte[buffer.limit()];
        buffer.get(payload);
        System.out.println(payload.length);
        return payload;
    }

    private String asHex(byte[] in){
        StringBuilder hex = new StringBuilder();
        for (byte i : in) {
            hex.append(" ").append(String.format("%02X", i));
        }
        return hex.toString();
    }



    public byte[] getNetworkAddr(long services, int[] ipv4Address, short port) {
        byte[] ipv6Bytes = new byte[16];

        for (int i = 0; i < 10; i++) {
            ipv6Bytes[i] = 0;
        }

        ipv6Bytes[10] = (byte) 0xFF;
        ipv6Bytes[11] = (byte) 0xFF;

        for (int i = 0; i < 4; i++) {
            ipv6Bytes[12 + i] = (byte) ipv4Address[i];
        }

        return ByteBuffer.allocate(Long.BYTES + ipv6Bytes.length + Short.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(services)
                .put(ipv6Bytes)
                .putShort(port)
                .array();
    }

    private void readInput(DataInputStream inputStream) {
        try {
            // Read the magic value
            Stream<Byte> byteStream = Stream.generate(() -> {
                try {
                    int nextByte = inputStream.read();
                    return nextByte != -1 ? Optional.of((byte) nextByte) : Optional.<Byte>empty();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
                .takeWhile(Optional::isPresent)
                .map(Optional::get);

            int magic = asLittleEndianInt(byteStream.limit(4).());
            System.out.println("Magic: " + Integer.toHexString(magic));

//            // Read the command
//            byte[] commandBytes = new byte[12];
//            inputStream.readFully(commandBytes);
//            String command = new String(commandBytes, StandardCharsets.US_ASCII).trim();
//            System.out.println("Command: " + command);
//
//            // Read the payload length
//            // int length = inputStream.readInt();
//            byte[] bLength = new byte[4];
//            inputStream.readFully(bLength);
//            int length = BytesConverter.of(bLength).num();
////            int length = readLittleEndianInt(inputStream);
//
//            System.out.println("Length: " + length);
//
//            // Read the checksum
//            byte[] checksum = new byte[4];
//            inputStream.readFully(checksum);
//            System.out.println("Checksum: " + BytesConverter.of(checksum).hex());
//
//            // Read the payload
//            byte[] payload = new byte[length];
//            inputStream.readFully(payload);
//            System.out.println("Payload: " + BytesConverter.of(payload).hex());
//            if(payload.length > 0) {
//                unpackPayload(payload);
//            }


        } catch (EOFException e) {
            System.out.println("===== END OF PAYLOAD =====");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int asLittleEndianInt(byte[] in) throws IOException {
        return ByteBuffer.wrap(in).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
//    public int readLittleEndianInt(DataInputStream inputStream) throws IOException {
//        byte[] bytes = new byte[4];
//        inputStream.readFully(bytes);
//        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
//    }

    public void unpackPayload(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);

        int version = buffer.getInt();
        System.out.println("Version: " + version);

        long services = buffer.getLong();
        System.out.println("Services: " + services);

        long timestamp = buffer.getLong();
        System.out.println("Timestamp: " + timestamp);

        // Unpack network address of the node receiving this message
        long servicesNode = buffer.getLong();
        byte[] ipAddrNode = new byte[16];
        buffer.get(ipAddrNode);
        int portNode = buffer.getShort() & 0xffff;
        System.out.println("Receiving Node: Services=" + servicesNode + ", IP=" + Arrays.toString(ipAddrNode) + ", Port=" + portNode);

        // Unpack network address of the node emitting this message
        long servicesPeer = buffer.getLong();
        byte[] ipAddrPeer = new byte[16];
        buffer.get(ipAddrPeer);
        int portPeer = buffer.getShort() & 0xffff;
        System.out.println("Emitting Node: Services=" + servicesPeer + ", IP=" + Arrays.toString(ipAddrPeer) + ", Port=" + portPeer);

        long nonce = buffer.getLong();
        System.out.println("Nonce: " + nonce);

        byte[] userAgentBytes = new byte[buffer.get()];
        buffer.get(userAgentBytes);
        String userAgent = new String(userAgentBytes, StandardCharsets.UTF_8);
        System.out.println("User Agent: " + userAgent);

        int startHeight = buffer.getInt();
        System.out.println("Start Height: " + startHeight);

        boolean relay = buffer.get() != 0;
        System.out.println("Relay: " + relay);
    }
}