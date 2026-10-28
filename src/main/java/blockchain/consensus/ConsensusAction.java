package blockchain.consensus;

import blockchain.model.Block;
import blockchain.model.Vote;

public sealed interface ConsensusAction {

    record BroadcastVote(Vote vote)
            implements ConsensusAction {
    }

    record FinalizeBlock(Block block)
            implements ConsensusAction {
    }

    record ScheduleTimeout(
            long height,
            int round,
            long delayMillis) implements ConsensusAction {
    }
}