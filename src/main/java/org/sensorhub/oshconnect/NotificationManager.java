package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.notification.INotificationDataStream;
import org.sensorhub.oshconnect.notification.INotificationNode;
import org.sensorhub.oshconnect.notification.INotificationSystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NotificationManager {
    private final Set<INotificationNode> nodeNotificationListeners = new HashSet<>();
    private final Set<INotificationSystem> systemNotificationListeners = new HashSet<>();
    private final Set<INotificationDataStream> dataStreamNotificationListeners = new HashSet<>();
    private final Map<OSHNode, INotificationSystem> systemNotificationListenersInternal = new HashMap<>();
    private final Map<OSHSystem, INotificationDataStream> dataStreamNotificationListenersInternal = new HashMap<>();

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
     * Create a data stream notification listener, to be used when a system is added to OSHConnect.
     *
     * @return The data stream notification listener.
     */
    private INotificationDataStream createDataStreamNotificationListener() {
        return new INotificationDataStream() {
            @Override
            public void onItemAdded(OSHDataStream item) {
                notifyDataStreamAdded(item);
            }

            @Override
            public void onItemRemoved(OSHDataStream item) {
                notifyDataStreamRemoved(item);
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
     * Listeners are notified when a system is added or removed from OSHConnect for any node.
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
     * Add a data stream notification listener.
     * Listeners are notified when a data stream is added or removed from OSHConnect for any system.
     *
     * @param listener The listener.
     */
    public void addDataStreamNotificationListener(INotificationDataStream listener) {
        dataStreamNotificationListeners.add(listener);
    }

    /**
     * Remove a data stream notification listener.
     *
     * @param listener The listener.
     */
    public void removeDataStreamNotificationListener(INotificationDataStream listener) {
        dataStreamNotificationListeners.remove(listener);
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
     * Subscribe to data stream notifications for the system.
     *
     * @param system The system.
     */
    void notifySystemAdded(OSHSystem system) {
        systemNotificationListeners.forEach(listener -> listener.onItemAdded(system));

        INotificationDataStream dataStreamListener = createDataStreamNotificationListener();
        system.addDataStreamNotificationListener(dataStreamListener);
        dataStreamNotificationListenersInternal.put(system, dataStreamListener);
    }

    /**
     * Notify listeners that a system has been removed.
     * Unsubscribe from data stream notifications for the system.
     *
     * @param system The system.
     */
    void notifySystemRemoved(OSHSystem system) {
        systemNotificationListeners.forEach(listener -> listener.onItemRemoved(system));

        INotificationDataStream dataStreamListener = dataStreamNotificationListenersInternal.remove(system);
        if (dataStreamListener != null) {
            system.removeDataStreamNotificationListener(dataStreamListener);
        }
    }

    /**
     * Notify listeners that a data stream has been added.
     *
     * @param dataStream The data stream.
     */
    void notifyDataStreamAdded(OSHDataStream dataStream) {
        dataStreamNotificationListeners.forEach(listener -> listener.onItemAdded(dataStream));
    }

    /**
     * Notify listeners that a data stream has been removed.
     *
     * @param dataStream The data stream.
     */
    void notifyDataStreamRemoved(OSHDataStream dataStream) {
        dataStreamNotificationListeners.forEach(listener -> listener.onItemRemoved(dataStream));
    }

    void shutdown() {
        nodeNotificationListeners.clear();
        systemNotificationListeners.clear();
        dataStreamNotificationListeners.clear();
        systemNotificationListenersInternal.clear();
        dataStreamNotificationListenersInternal.clear();
    }
}
