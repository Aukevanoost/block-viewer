package util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record ByteBufferFeed(ByteBuffer buffer) {
    public static ByteBufferFeed from(byte[] pool) { return new ByteBufferFeed(ByteBuffer.wrap(pool)); }
    public static ByteBufferFeed from(ByteBuffer feed) { return new ByteBufferFeed(feed); }

    public byte pullByte() { return buffer.get(); }
    public boolean pullBoolean() { return buffer.get() != 0; }

    public long pullLong() { return buffer.getLong(); }
    public short pullShort() { return buffer.getShort(); }
    public int pullInt32() { return buffer.getInt(); }
    public int pullInt8() { return buffer.get() & 0xFF; }
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

    public byte[] toArray() {
        return buffer.array();
    }

}
