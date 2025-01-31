package org.sensorhub.oshconnect;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.ConSysApiClientExtras;
import org.sensorhub.oshconnect.net.Protocol;
import org.sensorhub.oshconnect.notification.INotificationSystem;
import org.sensorhub.oshconnect.util.Utilities;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Class representing an OpenSensorHub server instance or node
 */
public class OSHNode {
    /**
     * The root URL of the OpenSensorHub server, i.e., localhost:8181/sensorhub
     */
    @Getter
    private final String sensorHubRoot;
    /**
     * Flag indicating if server is secured through TLS/SSL
     */
    @Getter
    private final boolean isSecure;
    /**
     * A unique id of the server, for configuration management.
     */
    @Getter
    private final UUID uniqueId;
    private final Set<INotificationSystem> systemNotificationListeners = new HashSet<>();
    private final Set<OSHSystem> systems = new HashSet<>();
    /**
     * Friendly name for the server.
     */
    @Getter
    @Setter
    private String name = "OSH Node";
    /**
     * The authorization token for the server.
     * This is a Base64 encoded string of the form "username:password".
     * May be null if the username or password were not provided.
     */
    @Getter
    private String authorizationToken;
    @Getter
    private String username;
    @Getter
    private String password;

    public OSHNode(String sensorHubRoot, boolean isSecure, String username, String password) {
        this(sensorHubRoot, isSecure, username, password, UUID.randomUUID());
    }

    public OSHNode(String sensorHubRoot, boolean isSecure, String username, String password, UUID uniqueId) {
        // Strip off the http:// or https:// prefix
        if (sensorHubRoot.startsWith("http://")) {
            sensorHubRoot = sensorHubRoot.substring(7);
        } else if (sensorHubRoot.startsWith("https://")) {
            sensorHubRoot = sensorHubRoot.substring(8);
        }

        this.sensorHubRoot = sensorHubRoot;
        this.isSecure = isSecure;
        this.uniqueId = uniqueId;
        setAuthorization(username, password);
    }

    /**
     * Create an OSHNode object from a JSON string.
     *
     * @param json The JSON string.
     * @return The OSHNode object.
     */
    public static OSHNode fromJson(String json) {
        return new Gson().fromJson(json, OSHNode.class);
    }

    /**
     * Discover systems belonging to this OpenSensorHub node.
     *
     * @return The list of systems.
     */
    public List<OSHSystem> discoverSystems() throws ExecutionException, InterruptedException {
        List<ISystemWithDesc> systemResources = getSystemResourcesFromServer();
        for (ISystemWithDesc systemResource : systemResources) {
            if (systems.stream().noneMatch(s -> s.getId().equals(systemResource.getId()))) {
                addSystem(systemResource);
            }
        }

        return getSystems();
    }

    /**
     * Create a system on the OpenSensorHub node and add it to the list of discovered systems.
     * If a system with the same UID already exists, it will be returned instead.
     *
     * @param physicalSystem The system resource.
     * @return The OSHSystem object for the created system or null if the system could not be created.
     */
    public OSHSystem createSystem(ISystemWithDesc physicalSystem) throws ExecutionException, InterruptedException {
        String uid = physicalSystem.getUniqueIdentifier();

        var system = getSystemByUid(uid);
        if (system != null) {
            return system;
        }

        String id = getConnectedSystemsApiClient().addSystem(physicalSystem).get();
        return getSystemById(id);
    }

    /**
     * Add a system to the list of systems and notify listeners.
     *
     * @param systemResource The system resource.
     * @return The OSHSystem object for the added system.
     */
    private OSHSystem addSystem(ISystemWithDesc systemResource) {
        if (systemResource == null) return null;

        OSHSystem system = new OSHSystem(this, systemResource);
        systems.add(system);
        notifySystemAdded(system);
        return system;
    }

    /**
     * Delete a system from the OpenSensorHub node and remove it from the list of discovered systems.
     *
     * @param system The system to delete.
     * @return True if the system was deleted successfully, false otherwise.
     */
    public boolean deleteSystem(OSHSystem system) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(getHTTPPrefix(), getSystemsEndpoint(), system.getId()));
        if (authorizationToken != null) {
            request.setAuthorizationToken(authorizationToken);
        }
        APIResponse response = request.delete();

        if (response.isSuccessful()) {
            systems.remove(system);
            notifySystemRemoved(system);
        }

        return response.isSuccessful();
    }

    /**
     * Connect to the OpenSensorHub node and get a list of systems.
     *
     * @return The list of systems.
     */
    private List<ISystemWithDesc> getSystemResourcesFromServer() throws ExecutionException, InterruptedException {
        var result = getConnectedSystemsApiClientExtras().getSystems();

        return result.get();
    }

    /**
     * Discover data streams belonging to the systems of this OpenSensorHub node.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many data streams to discover;
     * it is recommended to call {@link OSHSystem#discoverDataStreams()} on individual systems containing the data streams of interest.
     *
     * @return The list of data streams.
     */
    public List<OSHDataStream> discoverDataStreams() throws ExecutionException, InterruptedException {
        for (OSHSystem system : systems) {
            system.discoverDataStreams();
        }
        return getDataStreams();
    }

    /**
     * Discover control streams belonging to the systems of this OpenSensorHub node.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many control streams to discover;
     * it is recommended to call {@link OSHSystem#discoverControlStreams()} on individual systems containing the control streams of interest.
     *
     * @return The list of control streams.
     */
    public List<OSHControlStream> discoverControlStreams() throws ExecutionException, InterruptedException {
        for (OSHSystem system : systems) {
            system.discoverControlStreams();
        }
        return getControlStreams();
    }

    public String getHTTPPrefix() {
        return isSecure ? Protocol.HTTPS.getPrefix() : Protocol.HTTP.getPrefix();
    }

    public String getWSPrefix() {
        return isSecure ? Protocol.WSS.getPrefix() : Protocol.WS.getPrefix();
    }

    public String getApiEndpoint() {
        return Utilities.joinPath(sensorHubRoot, Service.API.getEndpoint());
    }

    public String getSystemsEndpoint() {
        return Utilities.joinPath(getApiEndpoint(), Service.SYSTEMS.getEndpoint());
    }

    public String getObservationsEndpoint() {
        return Utilities.joinPath(getApiEndpoint(), Service.OBSERVATIONS.getEndpoint());
    }

    /**
     * Get a list of discovered systems.
     *
     * @return The list of systems.
     */
    public List<OSHSystem> getSystems() {
        return List.copyOf(systems);
    }

    /**
     * Get a list of discovered data streams for all systems of this node.
     *
     * @return The list of data streams.
     */
    public List<OSHDataStream> getDataStreams() {
        List<OSHDataStream> dataStreams = new ArrayList<>();
        for (OSHSystem system : systems) {
            dataStreams.addAll(system.getDataStreams());
        }
        return dataStreams;
    }

    /**
     * Get a list of discovered control streams for all systems of this node.
     *
     * @return The list of control streams.
     */
    public List<OSHControlStream> getControlStreams() {
        List<OSHControlStream> controlStreams = new ArrayList<>();
        for (OSHSystem system : systems) {
            controlStreams.addAll(system.getControlStreams());
        }
        return controlStreams;
    }

    /**
     * Set the authorization for the node.
     *
     * @param username the username.
     * @param password the password.
     */
    public void setAuthorization(String username, String password) {
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            authorizationToken = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            this.username = username;
            this.password = password;
        } else {
            authorizationToken = null;
            this.username = null;
            this.password = null;
        }
    }

    /**
     * Get the JSON representation of this object.
     * Note: The list of systems is not included in the JSON representation.
     * After deserialization, the list of systems must be rediscovered.
     *
     * @return The JSON representation of this object.
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Add a system notification listener.
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
     * Notify listeners that a system has been added.
     *
     * @param system The system.
     */
    private void notifySystemAdded(OSHSystem system) {
        for (INotificationSystem listener : systemNotificationListeners) {
            listener.onItemAdded(system);
        }
    }

    /**
     * Notify listeners that a system has been removed.
     *
     * @param system The system.
     */
    private void notifySystemRemoved(OSHSystem system) {
        for (INotificationSystem listener : systemNotificationListeners) {
            listener.onItemRemoved(system);
        }
    }

    private OSHSystem getSystemByUid(String uid) throws ExecutionException, InterruptedException {
        // Check if this UID already exists and return it
        for (OSHSystem system : systems) {
            if (system.getSystemResource().getId().equals(uid)) {
                return system;
            }
        }

        // Check if this UID already exists on the server and return it
        var result = getConnectedSystemsApiClient().getSystemByUid(uid, ResourceFormat.JSON);
        if (result != null) {
            var systemResource = result.get();
            if (systemResource != null) {
                return addSystem(systemResource);
            }
        }

        return null;
    }

    private OSHSystem getSystemById(String id) throws ExecutionException, InterruptedException {
        if (id == null || id.isEmpty()) return null;

        // Check if this ID already exists and return it
        for (OSHSystem system : systems) {
            if (system.getId().equals(id)) {
                return system;
            }
        }

        var result = getConnectedSystemsApiClient().getSystemById(id, ResourceFormat.JSON);

        return addSystem(result.get());
    }

    public ConSysApiClient getConnectedSystemsApiClient() {
        var conSysBuilder = ConSysApiClient
                .newBuilder(Utilities.joinPath(getHTTPPrefix(), getApiEndpoint()));
        if (authorizationToken != null) {
            conSysBuilder.simpleAuth(username, password.toCharArray());
        }
        return conSysBuilder.build();
    }

    public ConSysApiClientExtras getConnectedSystemsApiClientExtras() {
        var conSysBuilder = ConSysApiClientExtras
                .newBuilder(Utilities.joinPath(getHTTPPrefix(), getApiEndpoint()));
        if (authorizationToken != null) {
            conSysBuilder.simpleAuth(username, password.toCharArray());
        }
        return conSysBuilder.build();
    }
}
