package org.sensorhub.oshconnect;

import java.util.*;

public class NodeManager {
    /**
     * Nodes added to OSHConnect.
     */
    private final Set<OSHNode> oshNodes = new HashSet<>();
    private final NotificationManager notificationManager;

    NodeManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    /**
     * Add a node to OSHConnect.
     * <p>
     * If a node with the same ID already exists, the node will not be added.
     *
     * @param oshNode The node to add.
     * @return True if the node was added, false otherwise.
     */
    public boolean addNode(OSHNode oshNode) {
        if (oshNode == null)
            return false;

        for (OSHNode node : oshNodes) {
            if (node.getUniqueId().equals(oshNode.getUniqueId()))
                return false;
        }

        oshNodes.add(oshNode);
        notificationManager.notifyNodeAdded(oshNode);
        return true;
    }

    /**
     * Add a collection of nodes to OSHConnect.
     *
     * @param oshNodes The nodes to add.
     */
    public void addNodes(Collection<OSHNode> oshNodes) {
        oshNodes.forEach(this::addNode);
    }

    /**
     * Remove a node from OSHConnect.
     *
     * @param oshNode The node to remove.
     */
    public void removeNode(OSHNode oshNode) {
        if (oshNodes.remove(oshNode)) {
            notificationManager.notifyNodeRemoved(oshNode);
        }
    }

    /**
     * Remove a node from OSHConnect by ID.
     *
     * @param nodeId The ID of the node to remove.
     */
    public void removeNode(UUID nodeId) {
        removeNode(getNode(nodeId));
    }

    /**
     * Remove all nodes from OSHConnect.
     */
    public void removeAllNodes() {
        new ArrayList<>(oshNodes).forEach(this::removeNode);
    }

    /**
     * Get a list of nodes in OSHConnect.
     *
     * @return A list of nodes.
     */
    public List<OSHNode> getNodes() {
        return new ArrayList<>(oshNodes);
    }

    /**
     * Get a node from OSHConnect by ID.
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

    void shutdown() {
        removeAllNodes();
    }
}
