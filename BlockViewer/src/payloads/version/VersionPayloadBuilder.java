package payloads.version;

import payloads.fragments.NodePayloadFragment;
import payloads.IPayloadBuilder;
import util.ByteBufferFeed;

public class VersionPayloadBuilder implements IPayloadBuilder {
    private int version;
    private long services;
    private long timestamp;
    private NodePayloadFragment receiver;
    private NodePayloadFragment sender;
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

    public VersionPayloadBuilder setReceiver(NodePayloadFragment receiver) {
        this.receiver = receiver;
        return this;
    }

    public VersionPayloadBuilder setSender(NodePayloadFragment sender) {
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

        receiver = NodePayloadFragment.from(feed);
        sender = NodePayloadFragment.from(feed);
        nonce = feed.pullLong();

        int userAgentSize = feed.pullInt8();
        userAgent = feed.pullString(userAgentSize);

        startHeight = feed.pullInt32();
        relay = feed.pullBoolean();

        return build();
    }

    public VersionPayload build() {
        return new VersionPayload(version, services, timestamp, receiver, sender, nonce, userAgent, startHeight, relay);
    }
}