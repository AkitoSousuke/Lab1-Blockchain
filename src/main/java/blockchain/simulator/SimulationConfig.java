package blockchain.simulator;

public record SimulationConfig(
        String chainId,
        int nodeCount,
        int validatorCount,
        long seed,
        long roundTimeoutMillis) {
    public SimulationConfig {
        if (chainId == null || chainId.isBlank()) {
            throw new IllegalArgumentException("Invalid chainId");
        }

        if (nodeCount < 8) {
            throw new IllegalArgumentException("Need at least 8 nodes");
        }

        if (validatorCount < 4 ||
                (validatorCount - 1) % 3 != 0 ||
                validatorCount > nodeCount) {
            throw new IllegalArgumentException("Invalid validator count");
        }

        if (roundTimeoutMillis <= 0) {
            throw new IllegalArgumentException("Invalid timeout");
        }
    }

    public int maxByzantine() {
        return (validatorCount - 1) / 3;
    }

    public int quorum() {
        return 2 * maxByzantine() + 1;
    }

    public static SimulationConfig defaultConfig() {
        return new SimulationConfig(
                "lab01",
                8,
                4,
                42L,
                1000L);
    }
}