package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record BytesConverter(byte[] raw){
    public static BytesConverter of(byte[] bytes) {
        return new BytesConverter(bytes);
    }

    private Stream<Byte> stream() {
        return IntStream.range(0, raw.length).mapToObj(i -> raw[i]);
    }

    public String hex() {
        return stream()
                .map(b -> String.format("%02x", b))
                .collect(Collectors.joining());
    }

    public Integer num() {
        return ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
