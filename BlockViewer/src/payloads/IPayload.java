package payloads;

import java.nio.ByteBuffer;

public interface IPayload {
    public ByteBuffer toBuffer();
}
