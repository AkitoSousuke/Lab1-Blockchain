package blockchain.crypto;

import blockchain.model.Transaction;
import blockchain.model.BlockHeader;
import blockchain.model.Vote;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class CanonicalEncoder {

    private CanonicalEncoder() {
    }

    public static byte[] encodeTransaction(Transaction tx) {
        return encode(out -> {
            writeString(out, tx.chainId());
            writeString(out, tx.senderId());
            writeString(out, tx.key());
            writeString(out, tx.value());
            out.writeLong(tx.nonce());
        });
    }

    public static byte[] encodeHeader(BlockHeader header) {
        return encode(out -> {
            writeString(out, header.chainId());
            out.writeLong(header.height());
            out.writeInt(header.round());
            writeString(out, header.parentHash());
            writeString(out, header.stateHash());
            writeString(out, header.proposerId());
        });
    }

    public static byte[] encodeVote(Vote vote) {
        return encode(out -> {
            writeString(out, vote.chainId());
            out.writeLong(vote.height());
            out.writeInt(vote.round());
            writeString(out, vote.phase().name());
            writeString(out, vote.blockHash());
            writeString(out, vote.validatorId());
        });
    }

    private static void writeString(
            DataOutputStream out,
            String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static byte[] encode(EncoderAction action) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            DataOutputStream out = new DataOutputStream(buffer);

            action.write(out);
            out.flush();

            return buffer.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @FunctionalInterface
    private interface EncoderAction {
        void write(DataOutputStream out) throws IOException;
    }
}