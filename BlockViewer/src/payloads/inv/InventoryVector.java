package payloads.inv;

public record InventoryVector (int type, byte[] hash) {
    public enum InventoryType {ERROR, MSG_TX, MSG_BLOCK, MSG_FILTERED_BLOCK, MSG_CMPCT_BLOCK, MSG_WITNESS_TX, MSG_WITNESS_BLOCK, MSG_FILTERED_WITNESS_BLOCK}

    public InventoryType invType() {
        return InventoryType.values()[type()];
    }
}