package message.payloads.ping;

import message.payloads.IPayloadBuilder;
import util.ByteBufferFeed;

public class PingPayloadBuilder implements IPayloadBuilder {

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
    private long nonce;
    public PingPayload from(ByteBufferFeed feed) {
        nonce = feed.pullLong();
        return build();
    }
    public PingPayload build() {
        return new PingPayload(nonce);
    }


}