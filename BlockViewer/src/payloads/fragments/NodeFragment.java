package payloads.fragments;

import payloads.IPayload;
import util.ByteBufferFeed;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public record NodeFragment(long services, byte[] ipv6Bytes, short port) implements IPayload {
    public static NodeFragment from(ByteBufferFeed feed) {
        long services = feed.pullLong();
        byte[] ip = feed.pullBytes(16);
        short port = feed.pullShort();

        return new NodeFragment(services, ip, port);
    }
    public static NodeFragment from(long services, String ipAddress, short port) throws UnknownHostException {
        byte[] address = InetAddress.getByName(ipAddress).getAddress();
        if (address.length == 4)  address = toIpv6(address);

        return new NodeFragment(services, address,  port);
    }

    public String ipv6() throws UnknownHostException {
        return Inet6Address.getByAddress(ipv6Bytes).getHostAddress();
    }

    private static byte[] toIpv6(byte[] ipv4) {
        byte[] ipv6 = Arrays.copyOf(ipv4, 16);
        ipv6[10] = (byte) 0xFF;
        ipv6[11] = (byte) 0xFF;
        return ipv6;
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(bufferSize())
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(services)
            .put(ipv6Bytes)
            .putShort(port)
            .flip();
    }
    public int bufferSize() {
        return Long.BYTES + ipv6Bytes.length + Short.BYTES;
    }

    public String toString() {
        return " VersionPayload {" +
                "\n\t\t\tversion = " + services +
                ",\n\t\t\ttimestamp = " + Arrays.toString(ipv6Bytes) +
                ",\n\t\t\treceiver = " + port +
                "\n\t\t}";
    }
}
