package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.net.websocket.DatastreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DatastreamHandler;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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
    private final Set<DatastreamHandler> datastreamHandlers = new HashSet<>();

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
     * Remove all nodes from the OSHConnect instance.
     */
    public void removeAllNodes() {
        oshNodes.clear();
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

    /**
     * Create a new datastream handler and add it to the OSHConnect instance.
     * No need to call {@link OSHConnect#addDatastreamHandler(DatastreamHandler)} after calling this method.
     *
     * @param onStreamUpdate The function to call when a datastream is updated.
     * @return The datastream handler.
     */
    public DatastreamHandler createDatastreamHandler(Consumer<DatastreamEventArgs> onStreamUpdate) {
        DatastreamHandler handler = new DatastreamHandler() {
            @Override
            public void onStreamUpdate(DatastreamEventArgs args) {
                onStreamUpdate.accept(args);
            }
        };
        addDatastreamHandler(handler);
        return handler;
    }

    /**
     * Add a datastream handler to the OSHConnect instance.
     * This method is used to add a datastream handler that was created outside of the OSHConnect instance.
     *
     * @param handler The datastream handler to add.
     */
    public void addDatastreamHandler(DatastreamHandler handler) {
        datastreamHandlers.add(handler);
    }

    /**
     * Get a list of datastream handlers associated with the OSHConnect instance.
     *
     * @return A list of datastream handlers.
     */
    public List<DatastreamHandler> getDatastreamHandlers() {
        return new ArrayList<>(datastreamHandlers);
    }

    /**
     * Shutdown the datastream handler and its associated datastreams,
     * and remove it from the OSHConnect instance.
     */
    public void shutdownDatastreamHandler(DatastreamHandler handler) {
        handler.shutdown();
        datastreamHandlers.remove(handler);
    }

    /**
     * Shutdown all datastream handlers and disconnect from all datastreams,
     * and remove them from the OSHConnect instance.
     */
    public void shutdownDatastreamHandlers() {
        datastreamHandlers.forEach(DatastreamHandler::shutdown);
        datastreamHandlers.clear();
    }

    /**
     * Shutdown all datastream handlers and remove all nodes.
     */
    public void shutdown() {
        shutdownDatastreamHandlers();
        removeAllNodes();
    }
}