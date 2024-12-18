package org.sensorhub.oshconnect.oshdatamodels;

import org.sensorhub.oshconnect.datamodels.DatastreamResource;
import org.sensorhub.oshconnect.datamodels.SystemResource;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.HttpRequestMethod;
import org.sensorhub.oshconnect.notification.INotificationDatastream;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    private final Set<INotificationDatastream> datastreamNotificationListeners = new HashSet<>();

    /**
     * Discover the datastreams associated with the system.
     *
     * @return The list of datastreams.
     */
    public List<OSHDatastream> discoverDataStreams() {
        APIRequest request = new APIRequest();
        request.setRequestMethod(HttpRequestMethod.GET);
        request.setUrl(parentNode.getHTTPPrefix() + getDatastreamsEndpoint());
        if (parentNode.getAuthorizationToken() != null) {
            request.setAuthorizationToken(parentNode.getAuthorizationToken());
        }

        APIResponse<DatastreamResource> response = request.execute(DatastreamResource.class);
        List<DatastreamResource> datastreamResources = response.getItems();

        for (DatastreamResource datastreamResource : datastreamResources) {
            if (datastreams.stream().noneMatch(ds -> ds.getDatastreamResource().getId().equals(datastreamResource.getId()))) {
                OSHDatastream datastream = new OSHDatastream(datastreamResource, this);
                datastreams.add(datastream);
                notifyDatastreamAdded(datastream);
            }
        }

        return getDatastreams();
    }

    /**
     * Get the endpoint for the datastreams of this system.
     *
     * @return The endpoint.
     */
    public String getDatastreamsEndpoint() {
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

    /**
     * Add a listener for datastream notifications.
     *
     * @param listener The listener.
     */
    public void addDatastreamNotificationListener(INotificationDatastream listener) {
        datastreamNotificationListeners.add(listener);
    }

    /**
     * Remove a listener for datastream notifications.
     *
     * @param listener The listener.
     */
    public void removeDatastreamNotificationListener(INotificationDatastream listener) {
        datastreamNotificationListeners.remove(listener);
    }

    /**
     * Notify listeners of a new datastream.
     *
     * @param datastream The datastream.
     */
    public void notifyDatastreamAdded(OSHDatastream datastream) {
        for (INotificationDatastream listener : datastreamNotificationListeners) {
            listener.onItemAdded(datastream);
        }
    }

    /**
     * Notify listeners of a removed datastream.
     *
     * @param datastream The datastream.
     */
    public void notifyDatastreamRemoved(OSHDatastream datastream) {
        for (INotificationDatastream listener : datastreamNotificationListeners) {
            listener.onItemRemoved(datastream);
        }
    }
}
