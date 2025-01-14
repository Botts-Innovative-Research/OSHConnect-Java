package org.sensorhub.oshconnect.oshdatamodels;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.datamodels.Observation;
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
    @Setter
    private String authorizationToken;

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

        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(getHTTPPrefix(), getApiEndpoint()))
                .build()
                .addSystem(physicalSystem);

        String id = conSys.get();
        return getSystemById(id);
    }

    /**
     * Add a system to the list of systems and notify listeners.
     *
     * @param systemResource The system resource.
     * @return The OSHSystem object for the added system.
     */
    private OSHSystem addSystem(ISystemWithDesc systemResource) {
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
        var conSys = ConSysApiClientExtras
                .newBuilder(Utilities.joinPath(getHTTPPrefix(), getApiEndpoint()))
                .build()
                .getSystems(ResourceFormat.JSON);

        System.out.println("asd");
        return conSys.get();
    }

    /**
     * Discover data streams belonging to the systems of this OpenSensorHub node.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many data streams to discover;
     * it is recommended to call {@link OSHSystem#discoverDataStreams()} on individual systems containing the datastreams of interest.
     *
     * @return The list of datastreams.
     */
    public List<OSHDatastream> discoverDatastreams() throws ExecutionException, InterruptedException {
        for (OSHSystem system : systems) {
            system.discoverDataStreams();
        }
        return getDatastreams();
    }

    /**
     * Discover control streams belonging to the systems of this OpenSensorHub node.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many control streams to discover;
     * it is recommended to call {@link OSHSystem#discoverControlStreams()} on individual systems containing the control streams of interest.
     *
     * @return The list of control streams.
     */
    public List<OSHControlStream> discoverControlStreams() {
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
     * Get a list of discovered datastreams for all systems of this node.
     *
     * @return The list of datastreams.
     */
    public List<OSHDatastream> getDatastreams() {
        List<OSHDatastream> datastreams = new ArrayList<>();
        for (OSHSystem system : systems) {
            datastreams.addAll(system.getDatastreams());
        }
        return datastreams;
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
        } else {
            authorizationToken = null;
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

    /**
     * Get an observation from the OpenSensorHub node.
     *
     * @param id The ID of the observation.
     * @return The observation as a JSON string.
     */
    public String getObservation(String id) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(getHTTPPrefix(), getObservationsEndpoint(), id));
        request.setAuthorizationToken(authorizationToken);
        APIResponse response = request.get();
        return response.getResponseBody();
    }

    /**
     * Get an observation from the OpenSensorHub node as an Observation object.
     * Note: This method will return null if the response body is not a JSON object.
     *
     * @param id The ID of the observation.
     * @return The observation as an Observation object.
     */
    public Observation getObservationObject(String id) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(getHTTPPrefix(), getObservationsEndpoint(), id));
        request.setAuthorizationToken(authorizationToken);
        APIResponse response = request.get();
        return response.getItem(Observation.class);
    }

    /**
     * Update the specified observation.
     *
     * @param id          The ID of the observation.
     * @param observation The observation object.
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateObservation(String id, Observation observation) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(getHTTPPrefix(), getObservationsEndpoint(), id));
        request.setBody(observation.toJson());
        request.setAuthorizationToken(authorizationToken);
        APIResponse response = request.put();
        return response.isSuccessful();
    }

    /**
     * Delete the specified observation.
     *
     * @param id The ID of the observation.
     * @return True if the deletion was successful, false otherwise.
     */
    public boolean deleteObservation(String id) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(getHTTPPrefix(), getObservationsEndpoint(), id));
        request.setAuthorizationToken(authorizationToken);
        APIResponse response = request.delete();
        return response.isSuccessful();
    }

    private OSHSystem getSystemByUid(String uid) throws ExecutionException, InterruptedException {
        // Check if this UID already exists and return it
        for (OSHSystem system : systems) {
            if (system.getSystemResource().getId().equals(uid)) {
                return system;
            }
        }

        // Check if this UID already exists on the server and return it
        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(getHTTPPrefix(), getApiEndpoint()))
                .build()
                .getSystemByUid(uid, ResourceFormat.JSON);
        if (conSys != null) {
            var systemResource = conSys.get();
            if (systemResource != null) {
                return addSystem(systemResource);
            }
        }

        return null;
    }

    private OSHSystem getSystemById(String id) throws ExecutionException, InterruptedException {
        // Check if this ID already exists and return it
        for (OSHSystem system : systems) {
            if (system.getId().equals(id)) {
                return system;
            }
        }

        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(getHTTPPrefix(), getApiEndpoint()))
                .build()
                .getSystemById(id, ResourceFormat.JSON);

        if (conSys != null) {
            return addSystem(conSys.get());
        }

        return null;
    }
}
