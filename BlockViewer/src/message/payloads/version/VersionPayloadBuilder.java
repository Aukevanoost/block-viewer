package message.payloads.version;

import message.payloads.fragments.NodeFragment;
import message.payloads.IPayloadBuilder;
import util.ByteBufferFeed;

public class VersionPayloadBuilder implements IPayloadBuilder {
    private int version;
    private long services;
    private long timestamp;
    private NodeFragment receiver;
    private NodeFragment sender;
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

    public VersionPayloadBuilder setReceiver(NodeFragment receiver) {
        this.receiver = receiver;
        return this;
    }

    public VersionPayloadBuilder setSender(NodeFragment sender) {
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


        public VersionPayload from(ByteBufferFeed feed) {
        version = feed.pullInt32();
        services = feed.pullLong();
        timestamp = feed.pullLong();

        receiver = NodeFragment.from(feed);
        sender = NodeFragment.from(feed);
        nonce = feed.pullLong();

        int userAgentSize = feed.pullVarInt();
        userAgent = feed.pullString(userAgentSize);

        startHeight = feed.pullInt32();
        relay = feed.pullBoolean();

        return build();
    }

    public VersionPayload build() {
        return new VersionPayload(version, services, timestamp, receiver, sender, nonce, userAgent, startHeight, relay);
    }
}