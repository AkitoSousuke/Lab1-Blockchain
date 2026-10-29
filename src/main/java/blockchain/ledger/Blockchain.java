package blockchain.ledger;

import blockchain.model.Block;
import java.util.List;

public interface Blockchain {

    // Danh sách block đã finalized, theo thứ tự height
    List<Block> finalizedBlocks();

    // Chiều cao block cuối cùng đã finalized
    long finalizedHeight();

    // Hash của block cuối cùng đã finalized
    String lastFinalizedHash();

    // Thêm block sau khi consensus xác nhận finality
    void appendFinalized(Block block);
}