package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.notification.INotificationDatastream;
import org.sensorhub.oshconnect.notification.INotificationNode;
import org.sensorhub.oshconnect.notification.INotificationSystem;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NotificationManager {
    private final Set<INotificationNode> nodeNotificationListeners = new HashSet<>();
    private final Set<INotificationSystem> systemNotificationListeners = new HashSet<>();
    private final Set<INotificationDatastream> datastreamNotificationListeners = new HashSet<>();
    private final Map<OSHNode, INotificationSystem> systemNotificationListenersInternal = new HashMap<>();
    private final Map<OSHSystem, INotificationDatastream> datastreamNotificationListenersInternal = new HashMap<>();

    /**
     * Package-private constructor, to be used by OSHConnect.
     */
    NotificationManager() {
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
    void notifyNodeAdded(OSHNode node) {
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
    void notifyNodeRemoved(OSHNode node) {
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
    void notifySystemAdded(OSHSystem system) {
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
    void notifySystemRemoved(OSHSystem system) {
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
    void notifyDatastreamAdded(OSHDatastream datastream) {
        datastreamNotificationListeners.forEach(listener -> listener.onItemAdded(datastream));
    }

    /**
     * Notify listeners that a datastream has been removed.
     *
     * @param datastream The datastream.
     */
    void notifyDatastreamRemoved(OSHDatastream datastream) {
        datastreamNotificationListeners.forEach(listener -> listener.onItemRemoved(datastream));
    }

    void shutdown() {
        nodeNotificationListeners.clear();
        systemNotificationListeners.clear();
        datastreamNotificationListeners.clear();
        systemNotificationListenersInternal.clear();
        datastreamNotificationListenersInternal.clear();
    }
}
