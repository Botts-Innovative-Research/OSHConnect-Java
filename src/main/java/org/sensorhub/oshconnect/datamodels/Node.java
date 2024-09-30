package org.sensorhub.oshconnect.datamodels;

import lombok.Getter;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.HttpRequestMethod;
import org.sensorhub.oshconnect.net.Protocol;
import org.sensorhub.oshconnect.time.TimePeriod;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Class representing an OpenSensorHub server instance or node
 */
@Getter
public class Node {
    /**
     * The root URL of the OpenSensorHub server, i.e. http://localhost:8181/sensorhub
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
     * The authorization token for the server.
     */
    private final String authorizationToken;

    /**
     * The time range for the OSHConnect instance.
     * This is used to bookend the playback of the datastreams.
     */
    private TimePeriod timePeriod;

    public Node(String sensorHubRoot, boolean isSecure) {
        this(sensorHubRoot, isSecure, null, null);
    }

    public Node(String sensorHubRoot, boolean isSecure, String username, String password) {
        this(sensorHubRoot, isSecure, username, password, UUID.randomUUID());
    }

    public Node(String sensorHubRoot, boolean isSecure, String username, String password, UUID uniqueId) {
        // Strip off the http:// or https:// prefix
        if (sensorHubRoot.startsWith("http://")) {
            sensorHubRoot = sensorHubRoot.substring(7);
        } else if (sensorHubRoot.startsWith("https://")) {
            sensorHubRoot = sensorHubRoot.substring(8);
        }

        this.sensorHubRoot = sensorHubRoot;
        this.uniqueId = uniqueId;
        this.isSecure = isSecure;

        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            authorizationToken = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        } else {
            authorizationToken = null;
        }
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
     * Discover systems from the nodes that have been added to the OSHConnect instance.
     *
     * @return a list of systems.
     */
    public List<System> discoverSystems() {
        APIRequest request = new APIRequest();
        request.setRequestMethod(HttpRequestMethod.GET);
        request.setUrl(getHTTPPrefix() + getSystemsEndpoint());
        if (authorizationToken != null) {
            request.setAuthorizationToken(authorizationToken);
        }

        APIResponse<System> response = request.execute(System.class);
        List<System> systems = response.getItems();
        systems.forEach(system -> system.setParentNode(this));
        return systems;
    }

    /**
     * Discover datastreams from the nodes that have been added to the OSHConnect instance.
     *
     * @return a list of datastreams.
     */
    public List<Datastream> discoverDatastreams() {
        var systems = discoverSystems();

        return systems.stream()
                .map(System::discoverDataStreams)
                .flatMap(List::stream)
                .toList();
    }
}
