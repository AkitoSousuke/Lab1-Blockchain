package blockchain.model;

/**
 * Một yêu cầu thay đổi dữ liệu trong blockchain.
 *
 * Ví dụ:
 * Alice muốn lưu key="message", value="Hello".
 *
 * Transaction phải được ký bởi người gửi.
 */
public record Transaction(
                String chainId,
                String senderId,
                String key,
                String value,
                long nonce,
                byte[] signature) {
}
