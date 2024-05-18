package message.payloads;

import util.ByteBufferFeed;

public interface IPayloadBuilder {
    IPayload build();
    IPayload from(ByteBufferFeed feed);
}
