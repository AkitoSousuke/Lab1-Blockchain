package blockchain.consensus;

import blockchain.model.Block;
import blockchain.model.Vote;

import java.util.List;

public interface ConsensusEngine {

    enum Step {
        PROPOSE, PREVOTE, PRECOMMIT
    }

    List<ConsensusAction> startHeight(long height);

    List<ConsensusAction> onProposal(
            Block block,
            boolean valid);

    List<ConsensusAction> onVote(Vote vote);

    List<ConsensusAction> onTimeout(
            long height,
            int round,
            Step step);

    /** Compatibility adapter; new callers must carry the timeout step. */
    @Deprecated
    default List<ConsensusAction> onTimeout(long height, int round) {
        return onTimeout(height, round, Step.PROPOSE);
    }

    ConsensusResult getResult();
}
