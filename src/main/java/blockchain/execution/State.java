package blockchain.execution;

import java.util.HashMap;
import java.util.Map;

public final class State {

    private final Map<String, String> data;
    private final Map<String, Long> nonces;

    public State() {
        this.data = new HashMap<>();
        this.nonces = new HashMap<>();
    }

    private State(
            Map<String, String> data,
            Map<String, Long> nonces) {
        this.data = new HashMap<>(data);
        this.nonces = new HashMap<>(nonces);
    }

    public String get(String key) {
        return data.get(key);
    }

    public long nextNonce(String senderId) {
        return nonces.getOrDefault(senderId, 0L);
    }

    public State withUpdate(
            String senderId,
            String key,
            String value) {
        State next = copy();

        next.data.put(key, value);
        next.nonces.put(
                senderId,
                Math.addExact(next.nextNonce(senderId), 1L));

        return next;
    }

    public Map<String, String> data() {
        return Map.copyOf(data);
    }

    public Map<String, Long> nonces() {
        return Map.copyOf(nonces);
    }

    public State copy() {
        return new State(data, nonces);
    }
}