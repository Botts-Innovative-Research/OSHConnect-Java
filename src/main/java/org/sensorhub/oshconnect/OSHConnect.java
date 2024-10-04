package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;
import org.sensorhub.oshconnect.time.TimeController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OSHConnect {
    /**
     * The name of the OSHConnect instance.
     */
    @Getter
    private final String name;
    private final Set<OSHNode> oshNodes = new HashSet<>();
    @Getter
    private final TimeController timeController = new TimeController();

    public OSHConnect() {
        this("OSH Connect");
    }

    /**
     * Add a node to the OSHConnect instance.
     *
     * @param oshNode The node to add.
     */
    public void addNode(OSHNode oshNode) {
        oshNodes.add(oshNode);
    }

    /**
     * Add a collection of nodes to the OSHConnect instance.
     *
     * @param oshNode The nodes to add.
     */
    public void addNodes(Collection<OSHNode> oshNode) {
        this.oshNodes.addAll(oshNode);
    }

    /**
     * Remove a node from the OSHConnect instance.
     *
     * @param nodeId The ID of the node to remove.
     */
    public void removeNode(UUID nodeId) {
        oshNodes.removeIf(node -> node.getUniqueId().equals(nodeId));
    }

    /**
     * Remove a node from the OSHConnect instance.
     *
     * @param oshNode The node to remove.
     */
    public void removeNode(OSHNode oshNode) {
        oshNodes.remove(oshNode);
    }

    /**
     * Get a list of nodes in the OSHConnect instance.
     *
     * @return A list of nodes.
     */
    public Collection<OSHNode> getNodes() {
        return new ArrayList<>(oshNodes);
    }

    /**
     * Get a node from the OSHConnect instance.
     *
     * @param nodeId The ID of the node to get.
     * @return The node with the given ID, or null if no such node exists.
     */
    public OSHNode getNode(UUID nodeId) {
        return oshNodes.stream()
                .filter(node -> node.getUniqueId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Discover systems belonging to all OpenSensorHub nodes previously added to the OSHConnect instance.
     */
    public void discoverSystems() {
        oshNodes.forEach(OSHNode::discoverSystems);
    }

    /**
     * Discover datastreams belonging to all systems previously discovered by the OSHConnect instance.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many systems and datastreams to discover;
     * it is recommended to call OSHSystem.discoverDataStreams() on individual systems containing the datastreams of interest.
     */
    public void discoverDatastreams() {
        oshNodes.forEach(OSHNode::discoverDatastreams);
    }

    /**
     * Get a list of all systems discovered by the OSHConnect instance.
     *
     * @return The list of systems.
     */
    public List<OSHSystem> getSystems() {
        List<OSHSystem> systems = new ArrayList<>();
        oshNodes.forEach(node -> systems.addAll(node.getSystems()));
        return systems;
    }

    /**
     * Get a list of all datastreams discovered by the OSHConnect instance.
     *
     * @return The list of datastreams.
     */
    public List<OSHDatastream> getDatastreams() {
        List<OSHDatastream> datastreams = new ArrayList<>();
        oshNodes.forEach(node -> datastreams.addAll(node.getDatastreams()));
        return datastreams;
    }
}