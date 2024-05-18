package util;

import payloads.IPayload;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class Convert {
    public static byte[] toVarInt(int value) {
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

        return Convert.toVarInt(payloads.size()).length + payloadSize;
    }

    public static byte[] hexToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)  sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
