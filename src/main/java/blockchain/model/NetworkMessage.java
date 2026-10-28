package blockchain.model;

import java.util.Objects;

public record NetworkMessage(
        String senderId,
        String receiverId,
        MessageType type,
        long height,
        Object payload) {
    public enum MessageType {
        PROPOSAL_HEADER,
        HEADER_ACCEPTED,
        PROPOSAL_BODY,
        PREVOTE,
        PRECOMMIT
    }

    public NetworkMessage {
        Objects.requireNonNull(senderId);
        Objects.requireNonNull(receiverId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(payload);
    }
}