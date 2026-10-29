package blockchain.model;

/**
 * Phiếu bầu của validator trong quá trình đồng thuận.
 *
 * Mỗi phiếu thuộc một trong hai giai đoạn:
 * PREVOTE hoặc PRECOMMIT.
 */
public record Vote(
        String chainId,
        long height,
        int round,
        Phase phase,
        String blockHash,
        String validatorId,
        byte[] signature) {
    public enum Phase {
        PREVOTE,
        PRECOMMIT
    }
}
