package payloads.fragments.node;

import payloads.IPayload;
import util.ByteBufferFeed;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public record NodePayloadFragment(long services, byte[] ipv6Bytes, short port) implements IPayload {
    public static NodePayloadFragment from(ByteBufferFeed feed) {
        long services = feed.pullLong();
        byte[] ip = feed.pullBytes(16);
        short port = feed.pullShort();

        return new NodePayloadFragment(services, ip, port);
    }
    public static NodePayloadFragment from(long services, String ipAddress, short port) throws UnknownHostException {
        byte[] address = InetAddress.getByName(ipAddress).getAddress();
        if (address.length == 4)  address = toIpv6(address);

        return new NodePayloadFragment(services, address,  port);
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
        return ByteBuffer.allocate(Long.BYTES + 16 + Short.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(services)
            .put(ipv6Bytes)
            .putShort(port)
            .flip();
    }
}
