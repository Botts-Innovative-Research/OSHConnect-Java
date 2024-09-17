package org.sensorhub.oshconnect.datamodels;

import lombok.Getter;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.HttpRequestMethod;
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

    public Node(String sensorHubRoot) {
        this(sensorHubRoot, null, null);
    }

    public Node(String sensorHubRoot, String username, String password) {
        this(sensorHubRoot, username, password, UUID.randomUUID());
    }

    public Node(String sensorHubRoot, String username, String password, UUID uniqueId) {
        this.sensorHubRoot = sensorHubRoot;
        this.uniqueId = uniqueId;

        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            authorizationToken = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        } else {
            authorizationToken = null;
        }
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
        request.setUrl(getSystemsEndpoint());
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
