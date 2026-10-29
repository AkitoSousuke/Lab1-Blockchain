package blockchain.crypto;

import java.security.KeyPair;
import java.security.PublicKey;

public interface CryptoService {

    KeyPair generateKeyPair();

    byte[] sign(
            byte[] message,
            String domain,
            java.security.PrivateKey privateKey);

    boolean verify(
            byte[] message,
            String domain,
            byte[] signature,
            PublicKey publicKey);

    String sha256(byte[] data);
}