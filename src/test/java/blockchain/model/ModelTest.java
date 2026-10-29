package blockchain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void testTransaction() {
        Transaction tx = new Transaction(
                "lab01",
                "Alice",
                "message",
                "Hello",
                1L,
                new byte[] { 1, 2, 3 });

        assertEquals("Alice", tx.senderId());
        assertEquals("Hello", tx.value());
        assertEquals(1L, tx.nonce());
    }

    @Test
    void testBlockHeader() {
        BlockHeader header = new BlockHeader(
                "lab01",
                1L,
                0,
                "genesis-hash",
                "state-hash",
                "validator-0",
                new byte[] { 1, 2, 3 });

        assertEquals(1L, header.height());
        assertEquals("genesis-hash", header.parentHash());
        assertEquals("validator-0", header.proposerId());
    }

    @Test
    void testVote() {
        Vote vote = new Vote(
                "lab01",
                1L,
                0,
                Vote.Phase.PREVOTE,
                "block-hash",
                "validator-1",
                new byte[] { 1, 2, 3 });

        assertEquals(Vote.Phase.PREVOTE, vote.phase());
        assertEquals("validator-1", vote.validatorId());
    }
}
