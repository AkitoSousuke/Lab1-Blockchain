package blockchain.model;

import java.util.List;

/**
 * Phần thông tin đầu của một block.
 *
 * Header liên kết block hiện tại với block trước đó
 * và xác nhận trạng thái dữ liệu sau khi thực thi.
 */
public record BlockHeader(
                String chainId,
                long height,
                int round,
                String parentHash,
                String stateHash,
                String proposerId,
                String transactionsHash,
                int validRound,
                List<Vote> prevoteProof,
                byte[] signature) {
    public BlockHeader {
        prevoteProof = List.copyOf(prevoteProof);
    }

    /**
     * Compatibility for the original record-construction tests.
     * The empty transactionsHash marks a header that is not protocol-valid.
     */
    @Deprecated
    public BlockHeader(
            String chainId,
            long height,
            int round,
            String parentHash,
            String stateHash,
            String proposerId,
            byte[] signature) {
        this(chainId, height, round, parentHash, stateHash, proposerId,
                "", -1, List.of(), signature);
    }
}
