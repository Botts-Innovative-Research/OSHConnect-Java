package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.config.ConfigManager;
import org.sensorhub.oshconnect.config.ConfigManagerJson;
import org.sensorhub.oshconnect.net.websocket.DatastreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DatastreamHandler;
import org.sensorhub.oshconnect.notification.INotificationDatastream;
import org.sensorhub.oshconnect.notification.INotificationNode;
import org.sensorhub.oshconnect.notification.INotificationSystem;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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
     * Datastream handlers added to the OSHConnect instance.
     */
    private final Set<DatastreamHandler> datastreamHandlers = new HashSet<>();
    private final Set<INotificationNode> nodeNotificationListeners = new HashSet<>();
    private final Set<INotificationSystem> systemNotificationListeners = new HashSet<>();
    private final Set<INotificationDatastream> datastreamNotificationListeners = new HashSet<>();
    private final Map<OSHNode, INotificationSystem> systemNotificationListenersInternal = new HashMap<>();
    private final Map<OSHSystem, INotificationDatastream> datastreamNotificationListenersInternal = new HashMap<>();

    /**
     * The configuration manager used to export and import configuration data.
     */
    @Getter
    @Setter
    private ConfigManager configManager;

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
     * Create a new OSHConnect instance with a given name and configuration manager.
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
        notifyNodeAdded(oshNode);
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
            notifyNodeRemoved(oshNode);
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
        List<OSHNode> nodesToRemove = getNodes();
        nodesToRemove.forEach(this::removeNode);
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

    /**
     * Create a system notification listener, to be used when a node is added to OSHConnect.
     *
     * @return The system notification listener.
     */
    private INotificationSystem createSystemNotificationListener() {
        return new INotificationSystem() {
            @Override
            public void onItemAdded(OSHSystem item) {
                notifySystemAdded(item);
            }

            @Override
            public void onItemRemoved(OSHSystem item) {
                notifySystemRemoved(item);
            }
        };
    }

    /**
     * Create a datastream notification listener, to be used when a system is added to OSHConnect.
     *
     * @return The datastream notification listener.
     */
    private INotificationDatastream createDatastreamNotificationListener() {
        return new INotificationDatastream() {
            @Override
            public void onItemAdded(OSHDatastream item) {
                notifyDatastreamAdded(item);
            }

            @Override
            public void onItemRemoved(OSHDatastream item) {
                notifyDatastreamRemoved(item);
            }
        };
    }

    /**
     * Add a node notification listener.
     * Listeners are notified when a node is added or removed from OSHConnect.
     *
     * @param listener The listener.
     */
    public void addNodeNotificationListener(INotificationNode listener) {
        nodeNotificationListeners.add(listener);
    }

    /**
     * Remove a node notification listener.
     *
     * @param listener The listener.
     */
    public void removeNodeNotificationListener(INotificationNode listener) {
        nodeNotificationListeners.remove(listener);
    }

    /**
     * Add a system notification listener.
     * Listeners are notified when a system is added or removed from OSHConnect, for any node.
     *
     * @param listener The listener.
     */
    public void addSystemNotificationListener(INotificationSystem listener) {
        systemNotificationListeners.add(listener);
    }

    /**
     * Remove a system notification listener.
     *
     * @param listener The listener.
     */
    public void removeSystemNotificationListener(INotificationSystem listener) {
        systemNotificationListeners.remove(listener);
    }

    /**
     * Add a datastream notification listener.
     * Listeners are notified when a datastream is added or removed from OSHConnect, for any system.
     *
     * @param listener The listener.
     */
    public void addDatastreamNotificationListener(INotificationDatastream listener) {
        datastreamNotificationListeners.add(listener);
    }

    /**
     * Remove a datastream notification listener.
     *
     * @param listener The listener.
     */
    public void removeDatastreamNotificationListener(INotificationDatastream listener) {
        datastreamNotificationListeners.remove(listener);
    }

    /**
     * Notify listeners that a node has been added.
     * Subscribe to system notifications for the node.
     *
     * @param node The node.
     */
    private void notifyNodeAdded(OSHNode node) {
        nodeNotificationListeners.forEach(listener -> listener.onItemAdded(node));

        INotificationSystem systemListener = createSystemNotificationListener();
        node.addSystemNotificationListener(systemListener);
        systemNotificationListenersInternal.put(node, systemListener);
    }

    /**
     * Notify listeners that a node has been removed.
     * Unsubscribe from system notifications for the node.
     *
     * @param node The node.
     */
    private void notifyNodeRemoved(OSHNode node) {
        nodeNotificationListeners.forEach(listener -> listener.onItemRemoved(node));

        INotificationSystem systemListener = systemNotificationListenersInternal.remove(node);
        if (systemListener != null) {
            node.removeSystemNotificationListener(systemListener);
        }
    }

    /**
     * Notify listeners that a system has been added.
     * Subscribe to datastream notifications for the system.
     *
     * @param system The system.
     */
    private void notifySystemAdded(OSHSystem system) {
        systemNotificationListeners.forEach(listener -> listener.onItemAdded(system));

        INotificationDatastream datastreamListener = createDatastreamNotificationListener();
        system.addDatastreamNotificationListener(datastreamListener);
        datastreamNotificationListenersInternal.put(system, datastreamListener);
    }

    /**
     * Notify listeners that a system has been removed.
     * Unsubscribe from datastream notifications for the system.
     *
     * @param system The system.
     */
    private void notifySystemRemoved(OSHSystem system) {
        systemNotificationListeners.forEach(listener -> listener.onItemRemoved(system));

        INotificationDatastream datastreamListener = datastreamNotificationListenersInternal.remove(system);
        if (datastreamListener != null) {
            system.removeDatastreamNotificationListener(datastreamListener);
        }
    }

    /**
     * Notify listeners that a datastream has been added.
     *
     * @param datastream The datastream.
     */
    private void notifyDatastreamAdded(OSHDatastream datastream) {
        datastreamNotificationListeners.forEach(listener -> listener.onItemAdded(datastream));
    }

    /**
     * Notify listeners that a datastream has been removed.
     *
     * @param datastream The datastream.
     */
    private void notifyDatastreamRemoved(OSHDatastream datastream) {
        datastreamNotificationListeners.forEach(listener -> listener.onItemRemoved(datastream));
    }
}