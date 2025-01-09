package org.sensorhub.oshconnect.oshdatamodels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.datamodels.ControlStreamResource;
import org.sensorhub.oshconnect.datamodels.DatastreamResource;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.notification.INotificationControlStream;
import org.sensorhub.oshconnect.notification.INotificationDatastream;
import org.sensorhub.oshconnect.util.Utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing an OpenSensorHub system.
 */
@RequiredArgsConstructor
public class OSHSystem {
    @Getter
    private final OSHNode parentNode;
    private final Set<OSHDatastream> datastreams = new HashSet<>();
    private final Set<OSHControlStream> controlStreams = new HashSet<>();
    private final Set<INotificationDatastream> datastreamNotificationListeners = new HashSet<>();
    private final Set<INotificationControlStream> controlStreamNotificationListeners = new HashSet();
    @Getter
    private ISystemWithDesc systemResource;

    public OSHSystem(OSHNode parentNode, ISystemWithDesc systemResource) {
        this.parentNode = parentNode;
        this.systemResource = systemResource;
    }

    /**
     * Discover the datastreams associated with the system.
     *
     * @return The list of datastreams.
     */
    public List<OSHDatastream> discoverDataStreams() {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(parentNode.getHTTPPrefix(), getDatastreamsEndpoint()));
        request.setAuthorizationToken(parentNode.getAuthorizationToken());

        APIResponse response = request.get();
        List<DatastreamResource> datastreamResources = response.getItems(DatastreamResource.class);

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
     * Discover the control streams associated with the system.
     *
     * @return The list of control streams.
     */
    public List<OSHControlStream> discoverControlStreams() {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(parentNode.getHTTPPrefix(), getControlStreamsEndpoint()));
        request.setAuthorizationToken(parentNode.getAuthorizationToken());

        APIResponse response = request.get();
        List<ControlStreamResource> controlStreamResources = response.getItems(ControlStreamResource.class);

        for (ControlStreamResource controlStreamResource : controlStreamResources) {
            if (datastreams.stream().noneMatch(ds -> ds.getDatastreamResource().getId().equals(controlStreamResource.getId()))) {
                OSHControlStream controlStream = new OSHControlStream(controlStreamResource, this);
                controlStreams.add(controlStream);
                notifyControlStreamAdded(controlStream);
            }
        }

        return getControlStreams();
    }

    public boolean updateSystem(ISystemWithDesc systemResource) {
        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(parentNode.getHTTPPrefix(), parentNode.getApiEndpoint()))
                .build()
                .updateSystem(getId(), systemResource);

        boolean success = false;
        try {
            Integer result = conSys.get();
            if (result != null && result >= 200 && result < 300) {
                success = true;
            }
        } catch (Exception e) {
            return false;
        }

        if (success) {
            return refreshSystem();
        }

        return false;
    }

    /**
     * Refresh the system properties from the server.
     *
     * @return True if the refresh was successful, false otherwise.
     */
    public boolean refreshSystem() {
        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(parentNode.getHTTPPrefix(), parentNode.getApiEndpoint()))
                .build()
                .getSystemById(getId(), ResourceFormat.JSON);
        try {
            this.systemResource = conSys.get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create a new datastream associated with the system.
     *
     * @param datastreamResource The datastream properties.
     * @return The new datastream or null if the creation failed.
     */
    public OSHDatastream createDatastream(DatastreamResource datastreamResource) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(parentNode.getHTTPPrefix(), getDatastreamsEndpoint()));
        request.setBody(datastreamResource.toJson());
        request.setAuthorizationToken(parentNode.getAuthorizationToken());
        APIResponse response = request.post();
        if (response.isSuccessful()) {
            DatastreamResource newResource = response.getItem(DatastreamResource.class);
            OSHDatastream datastream = new OSHDatastream(newResource, this);
            datastreams.add(datastream);
            notifyDatastreamAdded(datastream);
            return datastream;
        } else {
            System.out.println("Failed to create datastream: " + response.getResponseMessage());
        }
        return null;
    }

    /**
     * Get the endpoint for the datastreams of this system.
     *
     * @return The endpoint.
     */
    public String getDatastreamsEndpoint() {
        return Utilities.joinPath(parentNode.getSystemsEndpoint(), systemResource.getId(), Service.DATASTREAMS.getEndpoint());
    }

    /**
     * Get the endpoint for the control streams of this system.
     *
     * @return The endpoint.
     */
    public String getControlStreamsEndpoint() {
        return Utilities.joinPath(parentNode.getSystemsEndpoint(), systemResource.getId(), Service.CONTROLSTREAMS.getEndpoint());
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
     * Get a list of discovered control streams associated with the system.
     *
     * @return The control streams.
     */
    public List<OSHControlStream> getControlStreams() {
        return List.copyOf(controlStreams);
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

    /**
     * Notify listeners of a new control stream.
     *
     * @param controlStream The control stream.
     */
    public void notifyControlStreamAdded(OSHControlStream controlStream) {
        for (INotificationControlStream listener : controlStreamNotificationListeners) {
            listener.onItemAdded(controlStream);
        }
    }

    /**
     * Notify listeners of a removed control stream.
     *
     * @param controlStream The control stream.
     */
    public void notifyControlStreamRemoved(OSHControlStream controlStream) {
        for (INotificationControlStream listener : controlStreamNotificationListeners) {
            listener.onItemRemoved(controlStream);
        }
    }
}
