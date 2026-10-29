package blockchain.ledger;

import blockchain.execution.State;
import blockchain.model.Block;
import blockchain.model.BlockHeader;

public interface BlockValidator {

    // Header-only checks before accepting a proposal body; no state execution.
    boolean isHeaderValid(
            BlockHeader header,
            Block parent,
            State parentState);

    boolean isValid(
            Block block,
            Block parent,
            State parentState);
}
