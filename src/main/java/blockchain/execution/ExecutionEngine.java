package blockchain.execution;

import blockchain.model.Transaction;
import java.util.List;

public interface ExecutionEngine {

    State execute(
            State currentState,
            List<Transaction> transactions);
}