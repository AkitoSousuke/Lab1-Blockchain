package blockchain.crypto;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCryptoServiceTest {

    private final CryptoService crypto = new DefaultCryptoService();

    @Test
    void validSignatureMustVerify() {
        KeyPair keys = crypto.generateKeyPair();
        byte[] message = "hello".getBytes(StandardCharsets.UTF_8);

        byte[] signature = crypto.sign(
                message, "TX:lab01", keys.getPrivate());

        assertTrue(crypto.verify(
                message, "TX:lab01", signature, keys.getPublic()));
    }

    @Test
    void wrongDomainMustFail() {
        KeyPair keys = crypto.generateKeyPair();
        byte[] message = "hello".getBytes(StandardCharsets.UTF_8);

        byte[] signature = crypto.sign(
                message, "TX:lab01", keys.getPrivate());

        assertFalse(crypto.verify(
                message, "VOTE:lab01", signature, keys.getPublic()));
    }

    @Test
    void modifiedMessageMustFail() {
        KeyPair keys = crypto.generateKeyPair();

        byte[] signature = crypto.sign(
                "hello".getBytes(StandardCharsets.UTF_8),
                "TX:lab01",
                keys.getPrivate());

        assertFalse(crypto.verify(
                "changed".getBytes(StandardCharsets.UTF_8),
                "TX:lab01",
                signature,
                keys.getPublic()));
    }
}