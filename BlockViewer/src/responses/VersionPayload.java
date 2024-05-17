package responses;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record VersionPayload(int version, long services, long timestamp, VersionNode receiver, VersionNode sender, long nonce, String userAgent, int startHeight, Boolean relay) {
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

    public static class VersionPayloadBuilder {
        private int version;
        private long services;
        private long timestamp;
        private VersionNode receiver;
        private VersionNode sender;
        private long nonce;
        private String userAgent;
        private int startHeight;
        private Boolean relay;

        public VersionPayloadBuilder setVersion(int version) {
            this.version = version;
            return this;
        }

        public VersionPayloadBuilder setServices(long services) {
            this.services = services;
            return this;
        }

        public VersionPayloadBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public VersionPayloadBuilder setReceiver(VersionNode receiver) {
            this.receiver = receiver;
            return this;
        }

        public VersionPayloadBuilder setSender(VersionNode sender) {
            this.sender = sender;
            return this;
        }

        public VersionPayloadBuilder setNonce(long nonce) {
            this.nonce = nonce;
            return this;
        }

        public VersionPayloadBuilder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public VersionPayloadBuilder setStartHeight(int startHeight) {
            this.startHeight = startHeight;
            return this;
        }

        public VersionPayloadBuilder setRelay(Boolean relay) {
            this.relay = relay;
            return this;
        }

        public VersionPayload from(ByteBuffer buffer) {
            version = buffer.getInt();
            services = buffer.getLong();
            timestamp = buffer.getLong();

            receiver = VersionNode.from(buffer);
            sender = VersionNode.from(buffer);

            nonce = buffer.getLong();

            byte[] userAgentBytes = new byte[buffer.get()];
            buffer.get(userAgentBytes);
            userAgent = new String(userAgentBytes, StandardCharsets.UTF_8);

            startHeight = buffer.getInt();

            relay = buffer.get() != 0;
            return build();
        }

        public VersionPayload build() {
            return new VersionPayload(version, services, timestamp, receiver, sender, nonce, userAgent, startHeight, relay);
        }
    }
}
