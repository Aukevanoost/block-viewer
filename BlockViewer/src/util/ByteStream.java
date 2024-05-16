package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ByteStream {
    private InputStream _stream;

    private ByteStream(InputStream stream) {
        this._stream = stream;
    }

    public static ByteStream of(InputStream inputStream) {
        return new ByteStream(inputStream);
    }

    public byte[] fetch(int n) throws IOException {
        return tap()
                .collect(
                    ByteArrayOutputStream::new,
                    ByteArrayOutputStream::write,
                    ByteStream::mergeByteStreams
                )
                .toByteArray();

    }

    public Stream<Byte> tap() {
        return Stream.generate(() -> {
            try {
                int nextByte = this._stream.read();
                return nextByte != -1 ? Optional.of((byte) nextByte) : Optional.<Byte>empty();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).takeWhile(Optional::isPresent).map(Optional::get);
    }

    private static void mergeByteStreams(ByteArrayOutputStream bos1, ByteArrayOutputStream bos2) {
        try {
            bos2.writeTo(bos1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
