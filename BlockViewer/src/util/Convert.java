package util;

import payloads.IPayload;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class Convert {
    public static byte[] intToVarInt(int value) {
        if (value < 0xFD) {
            return new byte[]{(byte) value};
        } else if (value <= 0xFFFF) {
            return ByteBuffer
                .allocate(3)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0xFD)
                .putShort((short) value)
                .array();
        } else {
            return ByteBuffer
                .allocate(5)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0xFE)
                .putInt(value)
                .array();
        }
    }

    public static int payloadListToBufferSize(List<? extends IPayload> payloads) {
        var payloadSize = payloads.stream().reduce(
                0,
                (totalBufferSize, pl) -> totalBufferSize + pl.bufferSize(),
                Integer::sum
        );

        return Convert.intToVarInt(payloads.size()).length + payloadSize;
    }
}
