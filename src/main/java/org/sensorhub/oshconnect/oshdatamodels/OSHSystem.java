package org.sensorhub.oshconnect.oshdatamodels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sensorhub.oshconnect.datamodels.DatastreamResource;
import org.sensorhub.oshconnect.datamodels.SystemResource;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.HttpRequestMethod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing an OpenSensorHub system.
 */
@RequiredArgsConstructor
public class OSHSystem {
    @Getter
    private final SystemResource systemResource;
    @Getter
    private final OSHNode parentNode;
    private final Set<OSHDatastream> datastreams = new HashSet<>();

    /**
     * Discover the datastreams associated with the system.
     */
    public void discoverDataStreams() {
        APIRequest request = new APIRequest();
        request.setRequestMethod(HttpRequestMethod.GET);
        request.setUrl(parentNode.getHTTPPrefix() + getDataStreamsEndpoint());
        if (parentNode.getAuthorizationToken() != null) {
            request.setAuthorizationToken(parentNode.getAuthorizationToken());
        }

        APIResponse<DatastreamResource> response = request.execute(DatastreamResource.class);
        List<DatastreamResource> datastreamResources = response.getItems();

        for (DatastreamResource datastreamResource : datastreamResources) {
            if (datastreams.stream().noneMatch(ds -> ds.getDatastreamResource().getId().equals(datastreamResource.getId()))) {
                datastreams.add(new OSHDatastream(datastreamResource, this));
            }
        }
    }

    /**
     * Get the endpoint for the datastreams of this system.
     *
     * @return The endpoint.
     */
    public String getDataStreamsEndpoint() {
        return parentNode.getSystemsEndpoint() + "/" + systemResource.getId() + "/datastreams";
    }

    /**
     * Get the ID of the system.
     *
     * @return The ID.
     */
    public String getId() {
        return systemResource.getId();
    }

    /**
     * Get a list of discovered datastreams associated with the system.
     *
     * @return The datastreams.
     */
    public List<OSHDatastream> getDatastreams() {
        return List.copyOf(datastreams);
    }
}
