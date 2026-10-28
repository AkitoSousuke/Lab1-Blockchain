package blockchain.model;

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
                byte[] signature) {
}
