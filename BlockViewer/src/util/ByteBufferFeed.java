package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record ByteBufferFeed(ByteBuffer buffer) {
    public static ByteBufferFeed from(byte[] pool) { return new ByteBufferFeed(ByteBuffer.wrap(pool).order(ByteOrder.LITTLE_ENDIAN)); }
    public static ByteBufferFeed from(ByteBuffer feed) { return new ByteBufferFeed(feed); }

    public byte pullByte() { return buffer.get(); }


    public byte[] pullBytes(int n) {
        var bytes = new byte[n];
        buffer.get(bytes);
        return bytes;
    }

    public String pullString(int n) {
        return new String(this.pullBytes(n), StandardCharsets.US_ASCII).trim();
    }
    private  String pullHexString(int n) {
        StringBuilder sb = new StringBuilder();
        for (byte b : pullBytes(n))  sb.append(String.format("%02x", b));
        return sb.toString();
    }


    /*
     * Booleans
     */
    public boolean pullBoolean() { return buffer.get() != 0; }
    public boolean pullOptionalBoolean() { // 0001 or not available
        if (buffer.remaining() < 2) return false;

        int rollback = buffer.position() ;

        if(buffer.get() == (byte) 0x00 && buffer.get() == (byte) 0x01) {
            return true;
        }

        buffer.position(rollback);
        return false;
    }


    /*
     * *** NUMERIC OUTPUT ***
     */
    public long pullLong() { return buffer.getLong(); }
    public short pullShort() { return buffer.getShort(); }
    public int pullInt32() { return buffer.getInt(); }
    public int pullInt8() { return Byte.toUnsignedInt(buffer.get()); }
    public int pullVarInt() {
        byte firstByte = buffer.get();

        return switch (firstByte) {
            case (byte) 0xFD -> Short.toUnsignedInt(pullShort());
            case (byte) 0xFE, (byte) 0xFF -> pullInt32();
            default -> Byte.toUnsignedInt(firstByte);
        };

    }

    public byte[] toArray() {
        return buffer.array();
    }

}
