package payloads.version;

import payloads.fragments.NodePayloadFragment;
import payloads.IPayload;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record VersionPayload(int version, long services, long timestamp, NodePayloadFragment receiver, NodePayloadFragment sender, long nonce, String userAgent, int startHeight, boolean relay) implements IPayload {
    public static VersionPayloadBuilder builder() {
        return new VersionPayloadBuilder();
    }

    public ByteBuffer toBuffer() {
        byte[] user_agent = userAgent.getBytes(StandardCharsets.UTF_8);

        return ByteBuffer
            .allocate(4 + 8 + 8 + 26 + 26 + 8 + 1 + user_agent.length + 4 + 1)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(version)
            .putLong(services)
            .putLong(timestamp)
            .put(receiver.toBuffer())
            .put(sender.toBuffer())
            .putLong(nonce)
            .put((byte) user_agent.length)
            .put(user_agent)
            .putInt(startHeight)
            .put((byte) (relay ? 1 : 0))
            .flip();
    }

    @Override
    public String toString() {
        return
            "\tVersionPayload {" +
                "\n\t\tversion = " + version +
                ",\n\t\tservices = " + services +
                ",\n\t\ttimestamp = " + timestamp +
                ",\n\t\treceiver = " + receiver +
                ",\n\t\tsender = " + sender +
                ",\n\t\tnonce = " + nonce +
                ",\n\t\tuserAgent = '" + userAgent + "'" +
                ",\n\t\tstartHeight = " + startHeight +
                ",\n\t\trelay = " + relay +
            "\n\t\n}";
    }

}
