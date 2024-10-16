package org.sensorhub.oshconnect.oshdatamodels;

import com.google.gson.Gson;

import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.datamodels.SystemResource;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.HttpRequestMethod;
import org.sensorhub.oshconnect.net.Protocol;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Class representing an OpenSensorHub server instance or node
 */
@Getter
public class OSHNode {
    /**
     * The root URL of the OpenSensorHub server, i.e. localhost:8181/sensorhub
     */
    private final String sensorHubRoot;
    /**
     * Flag indicating if server is secured through TLS/SSL
     */
    private final boolean isSecure;
    /**
     * A unique id of the server, for configuration management.
     */
    private final UUID uniqueId;
    /**
     * Friendly name for the server.
     */
    @Setter
    private String name = "OSH Node";
    /**
     * The authorization token for the server.
     */
    @Setter
    private String authorizationToken;

    @SuppressWarnings("java:S2065") // Transient warning. Gson obeys the transient keyword.
    private final transient Set<OSHSystem> systems = new HashSet<>();

    public OSHNode(String sensorHubRoot, boolean isSecure) {
        this(sensorHubRoot, isSecure, null, null);
    }

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
        this.uniqueId = uniqueId;
        this.isSecure = isSecure;
        setAuthorization(username, password);
    }

    /**
     * Discover systems belonging to this OpenSensorHub node.
     *
     * @return The list of systems.
     */
    public List<OSHSystem> discoverSystems() {
        APIRequest request = new APIRequest();
        request.setRequestMethod(HttpRequestMethod.GET);
        request.setUrl(getHTTPPrefix() + getSystemsEndpoint());
        if (authorizationToken != null) {
            request.setAuthorizationToken(authorizationToken);
        }

        APIResponse<SystemResource> response = request.execute(SystemResource.class);
        List<SystemResource> systemResources = response.getItems();

        for (SystemResource systemResource : systemResources) {
            if (systems.stream().noneMatch(s -> s.getId().equals(systemResource.getId()))) {
                systems.add(new OSHSystem(systemResource, this));
            }
        }

        return getSystems();
    }

    /**
     * Discover datastreams belonging to the systems of this OpenSensorHub node.
     * This method should be called after discoverSystems().
     * Note: This method may take a long time to complete if there are many systems and datastreams to discover;
     * it is recommended to call OSHSystem.discoverDataStreams() on individual systems containing the datastreams of interest.
     *
     * @return The list of datastreams.
     */
    public List<OSHDatastream> discoverDatastreams() {
        for (OSHSystem system : systems) {
            system.discoverDataStreams();
        }
        return getDatastreams();
    }

    public String getHTTPPrefix() {
        return isSecure ? Protocol.HTTPS.getPrefix() : Protocol.HTTP.getPrefix();
    }

    public String getWSPrefix() {
        return isSecure ? Protocol.WSS.getPrefix() : Protocol.WS.getPrefix();
    }

    public String getApiEndpoint() {
        return sensorHubRoot + "/" + Service.API;
    }

    public String getSystemsEndpoint() {
        return getApiEndpoint() + "/systems";
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
     * Create an OSHNode object from a JSON string.
     *
     * @param json The JSON string.
     * @return The OSHNode object.
     */
    public static OSHNode fromJson(String json) {
        return new Gson().fromJson(json, OSHNode.class);
    }
}
