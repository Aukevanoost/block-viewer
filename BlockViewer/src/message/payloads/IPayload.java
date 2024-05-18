package message.payloads;

import java.nio.ByteBuffer;

public interface IPayload {
    ByteBuffer toBuffer();
    int bufferSize();
}
