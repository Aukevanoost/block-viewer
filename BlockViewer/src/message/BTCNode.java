package message;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public record BTCNode(long services, byte[] ipv6Bytes, short port){
    public static BTCNode from(ByteBuffer buffer) {
        long services = buffer.getLong();
        byte[] ip = new byte[16]; buffer.get(ip);
        short port = buffer.getShort();

        return new BTCNode(services, ip, port);
    }

    public static BTCNode from(long services, String ipAddress, short port) throws UnknownHostException {
        byte[] address = InetAddress.getByName(ipAddress).getAddress();
        if (address.length == 4)  address = toIpv6(address);

        return new BTCNode(services, address,  port);
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
