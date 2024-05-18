package payloads.version;

import payloads.fragments.node.NodePayloadFragment;
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


}
