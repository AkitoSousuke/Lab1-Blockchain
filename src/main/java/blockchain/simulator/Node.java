package blockchain.simulator;

public class Node {
    private final String id;
    private final boolean validator;

    public Node(String id, boolean validator) {
        this.id = id;
        this.validator = validator;
    }

    public String getId() {
        return id;
    }

    public boolean isValidator() {
        return validator;
    }
}