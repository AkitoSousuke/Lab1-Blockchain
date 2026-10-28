package blockchain.consensus;

import blockchain.model.Block;
import blockchain.model.Vote;

import java.util.List;

public interface ConsensusEngine {

    List<ConsensusAction> onProposal(
            Block block,
            boolean valid);

    List<ConsensusAction> onVote(Vote vote);

    List<ConsensusAction> onTimeout(
            long height,
            int round);

    ConsensusResult getResult();
}