package blockchain.simulator;

import java.util.ArrayList;
import java.util.List;

public class SimulationRunner {

    public List<Node> createNodes() {
        List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            nodes.add(new Node("node-" + i, i < 4));
        }

        return List.copyOf(nodes);
    }
}