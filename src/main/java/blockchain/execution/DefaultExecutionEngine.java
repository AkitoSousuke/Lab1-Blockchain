package blockchain.execution;

import blockchain.model.Transaction;

import java.util.List;
import java.util.Objects;

public final class DefaultExecutionEngine
        implements ExecutionEngine {

    private final TransactionValidator validator;

    public DefaultExecutionEngine(
            TransactionValidator validator) {
        this.validator = Objects.requireNonNull(validator);
    }

    @Override
    public State execute(
            State currentState,
            List<Transaction> transactions) {
        Objects.requireNonNull(currentState);
        Objects.requireNonNull(transactions);

        State next = currentState.copy();

        for (Transaction tx : transactions) {
            if (!validator.isValid(tx, next)) {
                throw new IllegalArgumentException(
                        "Invalid transaction");
            }

            next = next.withUpdate(
                    tx.senderId(),
                    tx.key(),
                    tx.value());
        }

        return next;
    }
}