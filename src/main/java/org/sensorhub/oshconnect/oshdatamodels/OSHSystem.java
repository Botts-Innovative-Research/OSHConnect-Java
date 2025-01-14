package org.sensorhub.oshconnect.oshdatamodels;

import lombok.Getter;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.datamodels.ControlStreamResource;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.ConSysApiClientExtras;
import org.sensorhub.oshconnect.notification.INotificationControlStream;
import org.sensorhub.oshconnect.notification.INotificationDatastream;
import org.sensorhub.oshconnect.util.Utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Class representing an OpenSensorHub system.
 */
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
    public List<OSHDatastream> discoverDataStreams() throws ExecutionException, InterruptedException {
        var conSys = ConSysApiClientExtras
                .newBuilder(Utilities.joinPath(parentNode.getHTTPPrefix(), parentNode.getApiEndpoint()))
                .build()
                .getDatastreams(ResourceFormat.JSON);

        var streams = conSys.get();
        for (var entry : streams.entrySet()) {
            if (this.datastreams.stream().noneMatch(ds -> ds.getId().equals(entry.getKey()))) {
                OSHDatastream datastream = new OSHDatastream(this, entry.getKey(), entry.getValue());
                this.datastreams.add(datastream);
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

        // TODO: fix this
//        for (ControlStreamResource controlStreamResource : controlStreamResources) {
//            if (datastreams.stream().noneMatch(ds -> ds.getDatastreamResource().getId().equals(controlStreamResource.getId()))) {
//                OSHControlStream controlStream = new OSHControlStream(controlStreamResource, this);
//                controlStreams.add(controlStream);
//                notifyControlStreamAdded(controlStream);
//            }
//        }

        return getControlStreams();
    }

    public boolean updateSystem(ISystemWithDesc systemResource) throws ExecutionException, InterruptedException {
        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(parentNode.getHTTPPrefix(), parentNode.getApiEndpoint()))
                .build()
                .updateSystem(getId(), systemResource);

        boolean success = false;

        Integer result = conSys.get();
        if (result != null && result >= 200 && result < 300) {
            success = true;
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
    public boolean refreshSystem() throws ExecutionException, InterruptedException {
        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(parentNode.getHTTPPrefix(), parentNode.getApiEndpoint()))
                .build()
                .getSystemById(getId(), ResourceFormat.JSON);
        var newSystemResource = conSys.get();

        if (newSystemResource != null) {
            systemResource = newSystemResource;
        }

        return newSystemResource != null;
    }

    /**
     * Create a new datastream associated with the system.
     *
     * @param datastreamResource The datastream properties.
     * @return The new datastream or null if the creation failed.
     */
    public OSHDatastream createDatastream(DataStreamInfo datastreamResource) throws ExecutionException, InterruptedException {
        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(parentNode.getHTTPPrefix(), parentNode.getApiEndpoint()))
                .build()
                .addDataStream(getId(), datastreamResource);

        String id = conSys.get();
        if (id != null) {
            OSHDatastream datastream = new OSHDatastream(this, id, datastreamResource);
            datastreams.add(datastream);
            notifyDatastreamAdded(datastream);
            return datastream;
        }

        return null;
    }

    /**
     * Delete a datastream associated with the system.
     *
     * @param datastream The datastream to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    public boolean deleteDatastream(OSHDatastream datastream) throws ExecutionException, InterruptedException {
        var conSys = ConSysApiClientExtras
                .newBuilder(Utilities.joinPath(parentNode.getHTTPPrefix(), parentNode.getApiEndpoint()))
                .build()
                .deleteDatastream(datastream.getId());
        Integer result = conSys.get();

        boolean success = result != null && result >= 200 && result < 300;

        if (success) {
            datastreams.remove(datastream);
            notifyDatastreamRemoved(datastream);
        }

        return success;
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
