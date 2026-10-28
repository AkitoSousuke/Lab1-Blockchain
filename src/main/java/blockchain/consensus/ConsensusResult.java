package blockchain.consensus;

public record ConsensusResult(
        long height,
        int round,
        String blockHash,
        boolean finalized) {
}