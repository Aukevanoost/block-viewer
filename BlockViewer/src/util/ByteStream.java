package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ByteStream {
    private InputStream _stream;

    private ByteStream(InputStream stream) {
        this._stream = stream;
    }

    public static ByteStream of(InputStream inputStream) {
        return new ByteStream(inputStream);
    }

    public ByteBuffer buffer(int n) throws IOException {
        var buffer = ByteBuffer.allocate(n).order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i < n; i++) {
            int nextByte = this._stream.read();
            if (nextByte < 0) throw new IOException("Stream ended unexpectedly");
            buffer.put((byte) nextByte);
        }
        return buffer.flip();
    }

    public byte[] array(int n) throws IOException {
        return buffer(n).array();
    }

    public Stream<Byte> tap() {
        return Stream.generate(() -> {
            try {
                int nextByte = this._stream.read();
                return nextByte != -1 ? Optional.of((byte) nextByte) : Optional.<Byte>empty();
            } catch (IOException e) {
                System.out.println("SOMETHING WENT WRONG");
                e.printStackTrace();
                throw new UncheckedIOException(e);
            }
        }).takeWhile(Optional::isPresent).map(Optional::get);
    }
    public Stream<Byte> tap(int n) {
        return tap().limit(n);
    }

    public List<Byte> batch(int n) {
        return tap(n).toList();
    }

    public String hex(int n) {
        return tap(n)
                .map(b -> String.format("%02x", b))
                .collect(Collectors.joining());
    }


}
