package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.config.ConfigManager;
import org.sensorhub.oshconnect.config.ConfigManagerJson;
import org.sensorhub.oshconnect.oshdatamodels.OSHControlStream;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

/**
 * OSHConnect is the main class for connecting to OpenSensorHub servers and managing datastreams.
 */
@Getter
public class OSHConnect {
    /**
     * The name of the OSHConnect instance.
     */
    private final String name;
    /**
     * The configuration manager, used to export and import configuration data.
     */
    @Setter
    private ConfigManager configManager = new ConfigManagerJson(this);
    /**
     * The node manager, used to create and manage connections to OpenSensorHub servers.
     */
    private final NodeManager nodeManager;
    /**
     * The datastream manager, used to create and manage connections to datastreams.
     */
    private final DatastreamManager datastreamManager;
    /**
     * The notification manager, used to notify listeners of changes to nodes, systems, and datastreams.
     */
    private final NotificationManager notificationManager;

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
        this.datastreamManager = new DatastreamManager();
        this.notificationManager = new NotificationManager();
        this.nodeManager = new NodeManager(notificationManager);
    }

    /**
     * Create a new OSHNode instance and add it to OSHConnect.
     * <p>
     * If another node with the root URL already exists,
     * the existing node will be returned.
     *
     * @param sensorHubRoot The root URL of the OpenSensorHub server, e.g. localhost:8181/sensorhub.
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
    public List<OSHSystem> discoverSystems() {
        nodeManager.getNodes().forEach(OSHNode::discoverSystems);
        return getSystems();
    }

    /**
     * Discover data streams belonging to all systems previously discovered by OSHConnect.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many systems and datastreams to discover;
     * it is recommended to call {@link OSHSystem#discoverDataStreams()} on individual systems containing the datastreams of interest.
     *
     * @return A list of all data streams discovered by OSHConnect.
     */
    public List<OSHDatastream> discoverDatastreams() {
        nodeManager.getNodes().forEach(OSHNode::discoverDatastreams);
        return getDatastreams();
    }

    /**
     * Discover control streams belonging to all systems previously discovered by OSHConnect.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many systems and control streams to discover;
     * it is recommended to call {@link OSHSystem#discoverControlStreams()} on individual systems containing the control streams of interest.
     *
     * @return A list of all control streams discovered by OSHConnect.
     */
    public List<OSHControlStream> discoverControlStreams() {
        nodeManager.getNodes().forEach(OSHNode::discoverControlStreams);
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
     * Get a list of all datastreams discovered by OSHConnect.
     *
     * @return The list of datastreams.
     */
    public List<OSHDatastream> getDatastreams() {
        return nodeManager.getNodes().stream()
                .flatMap(node -> node.getDatastreams().stream())
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
     * Shutdown all datastreams and remove all nodes.
     */
    public void shutdown() {
        datastreamManager.shutdown();
        nodeManager.shutdown();
        notificationManager.shutdown();
    }
}