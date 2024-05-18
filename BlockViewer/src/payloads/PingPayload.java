package payloads;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record PingPayload(long nonce) {
    public static PingPayload.PingPayloadBuilder builder() {
        return new PingPayload.PingPayloadBuilder();
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer
            .allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(nonce)
            .flip();
    }

    public static class PingPayloadBuilder {

        public void setNonce(long nonce) {
            this.nonce = nonce;
        }

        private long nonce;
        public PingPayload from(ByteBuffer buffer) {
            nonce = buffer.getLong();
            return build();
        }
        public PingPayload build() {
            return new PingPayload(nonce);
        }


    }
}
