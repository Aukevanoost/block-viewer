package payloads.block;

import payloads.IPayload;

import java.util.List;

public record BlockPayload(int version, byte[] prev_block, byte[] merkle_root, int timestamp, int bits, int nonce, List<Object> transactions) {

}