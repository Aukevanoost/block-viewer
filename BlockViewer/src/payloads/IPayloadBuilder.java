package payloads;

import message.BTCMessage;
import payloads.inv.InvPayload;
import util.ByteBufferFeed;

public interface IPayloadBuilder {
    IPayload build();
    IPayload from(ByteBufferFeed feed);
}
