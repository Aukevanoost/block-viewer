package connection;

import responses.VersionNode;
import responses.VersionPayload;
import responses.VersionResponse;
import util.ByteStream;

import java.io.*;
import java.net.Socket;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BitcoinVersionRequest {
    public void connect(String ip) {
        int nodePort = 8333;  // Default Bitcoin network port

        try (Socket socket = new Socket(ip, nodePort)) {
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

    private byte[] constructVersionPayload(String addr) throws UnknownHostException{
        short port = 8333;
        long services = 1;

        var payload = VersionPayload.builder()
                .setVersion(60002)
                .setServices(services)
                .setTimestamp(System.currentTimeMillis() / 1000)
                .setReceiver(VersionNode.from(services, addr, port))
                .setSender(VersionNode.from(services, "89.100.241.33", port))
                .setNonce(69)
                .setUserAgent("/Satoshi:0.7.2/")
                .setStartHeight(0)
                .setRelay(true)
                .build();

        byte[] in = payload.toBuffer().array();
        System.out.println(in.length);
        return in;
    }


    private void readInput(DataInputStream inputStream) {
        try {
            var bytes = ByteStream.of(inputStream);
            VersionResponse response = VersionResponse.from(bytes);
            System.out.format(
                    "Magic: %s\nCmd: %s\nChecksum: %s\nPayload size: %s",
                    response.magic(),
                    response.command(),
                    response.checksum(),
                    response.length()
            );
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}