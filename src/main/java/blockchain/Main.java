package blockchain;

import blockchain.simulator.Node;
import blockchain.simulator.SimulationRunner;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        SimulationRunner runner = new SimulationRunner();

        List<Node> nodes = runner.createNodes();

        System.out.println("Simulation initialized");

        for (Node node : nodes) {
            System.out.printf(
                    "%s | validator=%s%n",
                    node.getId(),
                    node.isValidator());
        }
    }
}