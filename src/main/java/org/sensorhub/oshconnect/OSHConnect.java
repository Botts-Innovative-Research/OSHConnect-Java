package org.sensorhub.oshconnect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sensorhub.oshconnect.datamodels.Node;

import java.util.*;

@RequiredArgsConstructor
public class OSHConnect {
    /**
     * The name of the OSHConnect instance.
     */
    @Getter
    private final String name;
    private final Set<Node> nodes = new HashSet<>();

    public OSHConnect() {
        this("OSH Connect");
    }

    /**
     * Add a node to the OSHConnect instance.
     *
     * @param node The node to add.
     */
    public void addNode(Node node) {
        addNodes(List.of(node));
    }

    /**
     * Add a collection of nodes to the OSHConnect instance.
     *
     * @param nodes The nodes to add.
     */
    public void addNodes(Collection<Node> nodes) {
        this.nodes.addAll(nodes);
    }

    /**
     * Remove a node from the OSHConnect instance.
     *
     * @param nodeId The ID of the node to remove.
     */
    public void removeNode(UUID nodeId) {
        nodes.removeIf(node -> node.getUniqueId().equals(nodeId));
    }

    /**
     * Remove a node from the OSHConnect instance.
     *
     * @param node The node to remove.
     */
    public void removeNode(Node node) {
        nodes.remove(node);
    }

    /**
     * Get a list of nodes in the OSHConnect instance.
     *
     * @return A list of nodes.
     */
    public Collection<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * Get a node from the OSHConnect instance.
     *
     * @param nodeId The ID of the node to get.
     * @return The node with the given ID, or null if no such node exists.
     */
    public Node getNode(UUID nodeId) {
        return nodes.stream()
                .filter(node -> node.getUniqueId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
}