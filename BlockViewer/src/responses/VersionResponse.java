package responses;

import util.ByteStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public record VersionResponse(String magic, String command, int length, String checksum, VersionPayload payload) {
    public static VersionResponse from(ByteStream stream) throws IOException {
        String magic = asHex(stream.buffer(4).array());

        String command = new String(stream.buffer(12).array(), StandardCharsets.US_ASCII).trim();
        int length = stream.buffer(4).getInt();
        String checksum = stream.hex(4);

        var payload = VersionPayload.builder().from(stream.buffer(length));

        return new VersionResponse(magic, command, length, checksum, payload);
    }

    private static String asHex(byte[] in){
        StringBuilder hex = new StringBuilder();
        for (byte i : in) {
            hex.append(" ").append(String.format("%02X", i));
        }
        return hex.toString();
    }
}
