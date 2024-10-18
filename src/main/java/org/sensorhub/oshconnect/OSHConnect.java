package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.config.ConfigManager;
import org.sensorhub.oshconnect.config.ConfigManagerJson;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

/**
 * OSHConnect is the main class for connecting to OpenSensorHub servers and managing datastreams.
 */
public class OSHConnect {
    /**
     * The name of the OSHConnect instance.
     */
    @Getter
    private final String name;
    /**
     * Nodes added to the OSHConnect instance.
     */
    private final Set<OSHNode> oshNodes = new HashSet<>();

    /**
     * The configuration manager, used to export and import configuration data.
     */
    @Getter
    @Setter
    private ConfigManager configManager;
    /**
     * The datastream manager, used to create and manage connections to datastreams.
     */
    @Getter
    private final DatastreamManager datastreamManager = new DatastreamManager();
    /**
     * The notification manager, used to notify listeners of changes to nodes, systems, and datastreams.
     */
    @Getter
    private final NotificationManager notificationManager = new NotificationManager();

    /**
     * Create a new OSHConnect instance.
     */
    public OSHConnect() {
        this("OSH Connect");
    }

    /**
     * Create a new OSHConnect instance with the given name.
     *
     * @param name The name of the OSHConnect instance.
     */
    public OSHConnect(String name) {
        this(name, new ConfigManagerJson());
    }

    /**
     * Create a new OSHConnect instance with the given name and configuration manager.
     *
     * @param name          The name of the OSHConnect instance.
     * @param configManager The configuration manager to use.
     */
    public OSHConnect(String name, ConfigManager configManager) {
        this.name = name;
        this.configManager = configManager;
    }

    /**
     * Add a node to the OSHConnect instance.
     * <p>
     * If a node with the same ID already exists,
     * or if another node with the same name and root URL already exists,
     * the node will not be added.
     *
     * @param oshNode The node to add.
     */
    public void addNode(OSHNode oshNode) {
        if (oshNode == null)
            return;

        for (OSHNode node : oshNodes) {
            if (node.getUniqueId().equals(oshNode.getUniqueId()))
                return;
            if (node.getName().equals(oshNode.getName()) && node.getSensorHubRoot().equals(oshNode.getSensorHubRoot()))
                return;
        }

        oshNodes.add(oshNode);
        notificationManager.notifyNodeAdded(oshNode);
    }

    /**
     * Add a collection of nodes to the OSHConnect instance.
     *
     * @param oshNodes The nodes to add.
     */
    public void addNodes(Collection<OSHNode> oshNodes) {
        oshNodes.forEach(this::addNode);
    }

    /**
     * Remove a node from the OSHConnect instance.
     *
     * @param oshNode The node to remove.
     */
    public void removeNode(OSHNode oshNode) {
        if (oshNodes.remove(oshNode)) {
            notificationManager.notifyNodeRemoved(oshNode);
        }
    }

    /**
     * Remove a node from the OSHConnect instance.
     *
     * @param nodeId The ID of the node to remove.
     */
    public void removeNode(UUID nodeId) {
        removeNode(getNode(nodeId));
    }

    /**
     * Remove all nodes from the OSHConnect instance.
     */
    public void removeAllNodes() {
        new ArrayList<>(oshNodes).forEach(this::removeNode);
    }

    /**
     * Get a list of nodes in the OSHConnect instance.
     *
     * @return A list of nodes.
     */
    public List<OSHNode> getNodes() {
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
     *
     * @return A list of all systems discovered by the OSHConnect instance.
     */
    public List<OSHSystem> discoverSystems() {
        oshNodes.forEach(OSHNode::discoverSystems);
        return getSystems();
    }

    /**
     * Discover datastreams belonging to all systems previously discovered by the OSHConnect instance.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many systems and datastreams to discover;
     * it is recommended to call OSHSystem.discoverDataStreams() on individual systems containing the datastreams of interest.
     *
     * @return A list of all datastreams discovered by the OSHConnect instance.
     */
    public List<OSHDatastream> discoverDatastreams() {
        oshNodes.forEach(OSHNode::discoverDatastreams);
        return getDatastreams();
    }

    /**
     * Get a list of all systems discovered by the OSHConnect instance.
     *
     * @return The list of systems.
     */
    public List<OSHSystem> getSystems() {
        return oshNodes.stream()
                .flatMap(node -> node.getSystems().stream())
                .collect(Collectors.toList());
    }

    /**
     * Get a list of all datastreams discovered by the OSHConnect instance.
     *
     * @return The list of datastreams.
     */
    public List<OSHDatastream> getDatastreams() {
        return oshNodes.stream()
                .flatMap(node -> node.getDatastreams().stream())
                .collect(Collectors.toList());
    }

    /**
     * Shutdown all datastream handlers and remove all nodes.
     */
    public void shutdown() {
        datastreamManager.shutdown();
        notificationManager.shutdown();
        removeAllNodes();
    }
}