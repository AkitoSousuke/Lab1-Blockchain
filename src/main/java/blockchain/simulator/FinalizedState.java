package blockchain.simulator;

import blockchain.execution.State;
import blockchain.model.Block;

public record FinalizedState(
        Block lastBlock,
        State state) {
    public FinalizedState {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        state = state.copy();
    }
}