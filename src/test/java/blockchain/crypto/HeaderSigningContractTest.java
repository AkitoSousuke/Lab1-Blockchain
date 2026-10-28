package blockchain.crypto;

import blockchain.model.BlockHeader;
import blockchain.model.Vote;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeaderSigningContractTest {

    private final CryptoService crypto = new DefaultCryptoService();

    @Test
    void signatureBindsBodyCommitmentRoundAndPrevoteEvidence() {
        KeyPair keys = crypto.generateKeyPair();
        Vote proof = new Vote("lab01", 1, 0, Vote.Phase.PREVOTE,
                "block-id", "node-0", new byte[] { 1 });
        BlockHeader original = header(1, "transactions-a", 0, List.of(proof), new byte[0]);
        byte[] signature = crypto.sign(CanonicalEncoder.encodeHeader(original),
                "HEADER:lab01", keys.getPrivate());

        Vote changedProof = new Vote("lab01", 1, 0, Vote.Phase.PREVOTE,
                "block-id", "node-0", new byte[] { 2 });
        List<BlockHeader> altered = List.of(
                header(1, "transactions-b", 0, List.of(proof), new byte[0]),
                header(2, "transactions-a", 0, List.of(proof), new byte[0]),
                header(1, "transactions-a", -1, List.of(proof), new byte[0]),
                header(1, "transactions-a", 0, List.of(changedProof), new byte[0]));

        for (BlockHeader candidate : altered) {
            assertFalse(crypto.verify(CanonicalEncoder.encodeHeader(candidate),
                    "HEADER:lab01", signature, keys.getPublic()));
        }
    }

    @Test
    void signedHeaderCanBeVerifiedWithoutSigningItsOwnSignature() {
        KeyPair keys = crypto.generateKeyPair();
        BlockHeader unsigned = header(0, "transactions-a", -1, List.of(), new byte[0]);
        byte[] signature = crypto.sign(CanonicalEncoder.encodeHeader(unsigned),
                "HEADER:lab01", keys.getPrivate());
        BlockHeader signed = header(0, "transactions-a", -1, List.of(), signature);

        assertTrue(crypto.verify(CanonicalEncoder.encodeHeader(signed),
                "HEADER:lab01", signed.signature(), keys.getPublic()));
    }

    private static BlockHeader header(int round, String transactionsHash, int validRound,
                                      List<Vote> proof, byte[] signature) {
        return new BlockHeader("lab01", 1, round, "parent", "state", "node-0",
                transactionsHash, validRound, proof, signature);
    }
}
