package responses;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record VersionNode (long services, byte[] ipv6Bytes, short port){
    public static VersionNode from(ByteBuffer buffer) {
        long services = buffer.getLong();
        byte[] ip = new byte[16]; buffer.get(ip);
        short port = buffer.getShort();

        return new VersionNode(services, ip, port);
    }
    public static VersionNode from(long services, int[] ipv4Bytes, short port) {
        byte[] ipv6Address = new byte[16];

        for (int i = 0; i < 10; i++)
            ipv6Address[i] = 0;

        ipv6Address[10] = (byte) 0xFF;
        ipv6Address[11] = (byte) 0xFF;

        for (int i = 0; i < 4; i++)
            ipv6Address[12 + i] = (byte) ipv4Bytes[i];

        return new VersionNode(services, ipv6Address, port);
    }

    public static VersionNode from(long services, String ipAddress, short port) throws UnknownHostException {
        return new VersionNode(
                services,
                InetAddress.getByName(ipAddress).getAddress(),
                port
        );
    }

    public String ipv6() throws UnknownHostException {
        return Inet6Address.getByAddress(ipv6Bytes).getHostAddress();
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
