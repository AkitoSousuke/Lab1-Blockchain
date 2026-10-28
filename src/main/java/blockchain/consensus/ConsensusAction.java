package blockchain.consensus;

import blockchain.model.Block;
import blockchain.model.Vote;

import java.util.List;

public sealed interface ConsensusAction {

    record BroadcastVote(Vote vote)
            implements ConsensusAction {
    }

    record FinalizeBlock(Block block, List<Vote> commitProof)
            implements ConsensusAction {
        public FinalizeBlock {
            commitProof = List.copyOf(commitProof);
        }

        /** Legacy construction only; an empty proof cannot authorize finality. */
        @Deprecated
        public FinalizeBlock(Block block) {
            this(block, List.of());
        }
    }

    record RequestProposal(
            long height,
            int round,
            String blockHash,
            int validRound,
            List<Vote> prevoteProof) implements ConsensusAction {
        public RequestProposal {
            prevoteProof = List.copyOf(prevoteProof);
        }
    }

    record ScheduleTimeout(
            long height,
            int round,
            ConsensusEngine.Step step,
            long delayMillis) implements ConsensusAction {
        /** Compatibility adapter; new callers must specify the timeout step. */
        @Deprecated
        public ScheduleTimeout(long height, int round, long delayMillis) {
            this(height, round, ConsensusEngine.Step.PROPOSE, delayMillis);
        }
    }
}
