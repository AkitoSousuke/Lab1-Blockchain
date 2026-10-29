package blockchain.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Objects;
import java.util.HexFormat;

public final class DefaultCryptoService implements CryptoService {

    private byte[] payload(byte[] message, String domain) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(domain);

        byte[] domainBytes = domain.getBytes(StandardCharsets.UTF_8);

        return ByteBuffer.allocate(4 + domainBytes.length + message.length)
                .putInt(domainBytes.length)
                .put(domainBytes)
                .put(message)
                .array();
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public byte[] sign(
            byte[] message,
            String domain,
            PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("Ed25519");
            signer.initSign(privateKey);
            signer.update(payload(message, domain));
            return signer.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean verify(
            byte[] message,
            String domain,
            byte[] signature,
            PublicKey publicKey) {
        try {
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(publicKey);
            verifier.update(payload(message, domain));
            return verifier.verify(signature);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public String sha256(byte[] data) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(data);

            return HexFormat.of().formatHex(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}