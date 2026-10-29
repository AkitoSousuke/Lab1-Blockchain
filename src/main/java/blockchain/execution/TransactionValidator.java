package blockchain.execution;

import blockchain.model.Transaction;
import blockchain.crypto.CanonicalEncoder;
import blockchain.crypto.CryptoService;

import java.security.PublicKey;
import java.util.Map;
import java.util.Objects;

public final class TransactionValidator {

    private final CryptoService crypto;
    private final Map<String, PublicKey> publicKeys;

    public TransactionValidator(
            CryptoService crypto,
            Map<String, PublicKey> publicKeys) {
        this.crypto = Objects.requireNonNull(crypto);
        this.publicKeys = Map.copyOf(publicKeys);
    }

    public boolean isValid(Transaction tx, State state) {
        if (tx == null || state == null) {
            return false;
        }

        if (!"lab01".equals(tx.chainId())) {
            return false;
        }

        PublicKey publicKey = publicKeys.get(tx.senderId());

        if (publicKey == null) {
            return false;
        }

        if (tx.key() == null ||
                !tx.key().startsWith(tx.senderId() + "/")) {
            return false;
        }

        if (tx.nonce() != state.nextNonce(tx.senderId())) {
            return false;
        }

        try {
            return crypto.verify(
                    CanonicalEncoder.encodeTransaction(tx),
                    "TX:lab01",
                    tx.signature(),
                    publicKey);
        } catch (RuntimeException e) {
            return false;
        }
    }
}