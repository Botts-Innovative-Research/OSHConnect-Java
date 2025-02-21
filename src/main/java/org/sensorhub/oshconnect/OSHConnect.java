package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.config.ConfigManager;
import org.sensorhub.oshconnect.config.ConfigManagerJson;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * OSHConnect is the main class for connecting to OpenSensorHub servers and managing data streams.
 */
public class OSHConnect {
    /**
     * The name of the OSHConnect instance.
     */
    private final String name;
    /**
     * The node manager, used to create and manage connections to OpenSensorHub servers.
     */
    private final NodeManager nodeManager;
    /**
     * The data stream manager, used to create and manage connections to data streams.
     */
    private final StreamManager dataStreamManager;
    /**
     * The control stream manager, used to create and manage connections to control streams.
     */
    private final StreamManager controlStreamManager;
    /**
     * The notification manager, used to notify listeners of changes to nodes, systems, and data streams.
     */
    private final NotificationManager notificationManager;
    /**
     * The configuration manager, used to export and import configuration data.
     */
    private ConfigManager configManager = new ConfigManagerJson(this);

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
        this.name = name;
        this.dataStreamManager = new StreamManager();
        this.controlStreamManager = new StreamManager();
        this.notificationManager = new NotificationManager();
        this.nodeManager = new NodeManager(notificationManager);
    }

    /**
     * Create a new OSHNode instance and add it to OSHConnect.
     * <p>
     * If another node with the root URL already exists,
     * the existing node will be returned.
     *
     * @param sensorHubRoot The root URL of the OpenSensorHub server, e.g., localhost:8181/sensorhub.
     * @param isSecure      Flag indicating if the server is secured through TLS/SSL.
     * @param username      The username for the server, if authentication is required.
     * @param password      The password for the server, if authentication is required.
     * @return The OSHNode instance.
     */
    public OSHNode createNode(String sensorHubRoot, boolean isSecure, String username, String password) {
        OSHNode node = new OSHNode(sensorHubRoot, isSecure, username, password);
        if (nodeManager.addNode(node)) {
            return node;
        }
        return null;
    }

    /**
     * Discover systems belonging to all OpenSensorHub nodes previously added to OSHConnect.
     *
     * @return A list of all systems discovered by OSHConnect.
     */
    public List<OSHSystem> discoverSystems() throws ExecutionException, InterruptedException {
        for (OSHNode node : nodeManager.getNodes()) {
            node.discoverSystems();
        }
        return getSystems();
    }

    /**
     * Discover data streams belonging to all systems previously discovered by OSHConnect.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many systems and data streams to discover;
     * it is recommended to call {@link OSHSystem#discoverDataStreams()} on individual systems containing the data streams of interest.
     *
     * @return A list of all data streams discovered by OSHConnect.
     */
    public List<OSHDataStream> discoverDataStreams() throws ExecutionException, InterruptedException {
        for (OSHNode node : nodeManager.getNodes()) {
            for (OSHSystem system : node.getSystems()) {
                system.discoverDataStreams();
            }
        }
        return getDataStreams();
    }

    /**
     * Discover control streams belonging to all systems previously discovered by OSHConnect.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many systems and control streams to discover;
     * it is recommended to call {@link OSHSystem#discoverControlStreams()} on individual systems containing the control streams of interest.
     *
     * @return A list of all control streams discovered by OSHConnect.
     */
    public List<OSHControlStream> discoverControlStreams() throws ExecutionException, InterruptedException {
        for (OSHNode node : nodeManager.getNodes()) {
            for (OSHSystem system : node.getSystems()) {
                system.discoverControlStreams();
            }
        }
        return getControlStreams();
    }

    /**
     * Get a list of all systems discovered by OSHConnect.
     *
     * @return The list of systems.
     */
    public List<OSHSystem> getSystems() {
        return nodeManager.getNodes().stream()
                .flatMap(node -> node.getSystems().stream())
                .collect(Collectors.toList());
    }

    /**
     * Get a list of all data streams discovered by OSHConnect.
     *
     * @return The list of data streams.
     */
    public List<OSHDataStream> getDataStreams() {
        return nodeManager.getNodes().stream()
                .flatMap(node -> node.getDataStreams().stream())
                .collect(Collectors.toList());
    }

    /**
     * Get a list of all control streams discovered by OSHConnect.
     *
     * @return The list of control streams.
     */
    public List<OSHControlStream> getControlStreams() {
        return nodeManager.getNodes().stream()
                .flatMap(node -> node.getControlStreams().stream())
                .collect(Collectors.toList());
    }

    /**
     * Shutdown all data streams and remove all nodes.
     */
    public void shutdown() {
        dataStreamManager.shutdown();
        nodeManager.shutdown();
        notificationManager.shutdown();
    }

    /**
     * The name of the OSHConnect instance.
     */
    public String getName() {
        return name;
    }

    /**
     * The node manager, used to create and manage connections to OpenSensorHub servers.
     */
    public NodeManager getNodeManager() {
        return nodeManager;
    }

    /**
     * The data stream manager, used to create and manage connections to data streams.
     */
    public StreamManager getDataStreamManager() {
        return dataStreamManager;
    }

    /**
     * The control stream manager, used to create and manage connections to control streams.
     */
    public StreamManager getControlStreamManager() {
        return controlStreamManager;
    }

    /**
     * The notification manager, used to notify listeners of changes to nodes, systems, and data streams.
     */
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    /**
     * The configuration manager, used to export and import configuration data.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * The configuration manager, used to export and import configuration data.
     * Note: The default configuration manager is {@link ConfigManagerJson}.
     * This method allows the user to set a custom configuration manager.
     */
    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}