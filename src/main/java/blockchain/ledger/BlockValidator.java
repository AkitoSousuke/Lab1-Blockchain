package blockchain.ledger;

import blockchain.execution.State;
import blockchain.model.Block;

public interface BlockValidator {

    boolean isValid(
            Block block,
            Block parent,
            State parentState);
}