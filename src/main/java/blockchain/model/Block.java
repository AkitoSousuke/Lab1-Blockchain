package blockchain.model;

import java.util.List;
import java.util.Objects;

public record Block(
        BlockHeader header,
        List<Transaction> transactions) {
    public Block {
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(transactions, "transactions");
        transactions = List.copyOf(transactions);
    }
}