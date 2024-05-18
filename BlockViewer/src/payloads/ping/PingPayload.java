package payloads.ping;

import payloads.IPayload;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record PingPayload(long nonce) implements IPayload {
    public static PingPayloadBuilder builder() {
        return new PingPayloadBuilder();
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer
            .allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(nonce)
            .flip();
    }


}
