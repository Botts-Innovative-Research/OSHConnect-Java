package org.sensorhub.oshconnect;

import lombok.Getter;
import org.sensorhub.api.command.CommandStreamInfo;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.net.ConSysApiClientExtras;
import org.sensorhub.oshconnect.notification.INotificationControlStream;
import org.sensorhub.oshconnect.notification.INotificationDataStream;
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
    private final Set<OSHDataStream> dataStreams = new HashSet<>();
    private final Set<OSHControlStream> controlStreams = new HashSet<>();
    private final Set<INotificationDataStream> dataStreamNotificationListeners = new HashSet<>();
    private final Set<INotificationControlStream> controlStreamNotificationListeners = new HashSet<>();
    @Getter
    private ISystemWithDesc systemResource;

    public OSHSystem(OSHNode parentNode, ISystemWithDesc systemResource) {
        this.parentNode = parentNode;
        this.systemResource = systemResource;
    }

    /**
     * Discover the data streams associated with the system.
     *
     * @return The list of data streams.
     */
    public List<OSHDataStream> discoverDataStreams() throws ExecutionException, InterruptedException {
        var dataStreamIds = getConnectedSystemsApiClientExtras().getDataStreamIds(getId()).get();
        for (var id : dataStreamIds) {
            if (dataStreams.stream().noneMatch(ds -> ds.getId().equals(id))) {
                var dataStreamResource = getConnectedSystemsApiClient().getDatastreamById(id, ResourceFormat.JSON, true).get();
                if (dataStreamResource != null) {
                    OSHDataStream dataStream = new OSHDataStream(this, id, dataStreamResource);
                    dataStreams.add(dataStream);
                    notifyDataStreamAdded(dataStream);
                }
            }
        }
        return getDataStreams();
    }

    /**
     * Discover the control streams associated with the system.
     *
     * @return The list of control streams.
     */
    public List<OSHControlStream> discoverControlStreams() throws ExecutionException, InterruptedException {
        var controlStreamIds = getConnectedSystemsApiClientExtras().getControlStreamIds(getId()).get();
        for (var id : controlStreamIds) {
            if (controlStreams.stream().noneMatch(cs -> cs.getId().equals(id))) {
                var controlStreamResource = getConnectedSystemsApiClient().getControlStreamById(id, ResourceFormat.JSON, true).get();
                if (controlStreamResource != null) {
                    OSHControlStream controlStream = new OSHControlStream(this, id, controlStreamResource);
                    controlStreams.add(controlStream);
                    notifyControlStreamAdded(controlStream);
                }
            }
        }
        return getControlStreams();
    }

    /**
     * Update the system properties on the server.
     * Note: After updating the system, the system properties are refreshed from the server,
     * not set to the provided system properties.
     *
     * @param systemResource The new system properties.
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateSystem(ISystemWithDesc systemResource) throws ExecutionException, InterruptedException {
        Integer response = getConnectedSystemsApiClient().updateSystem(getId(), systemResource).get();
        boolean success = response != null && response >= 200 && response < 300;

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
        var newSystemResource = getConnectedSystemsApiClient().getSystemById(getId(), ResourceFormat.JSON).get();

        if (newSystemResource != null) {
            systemResource = newSystemResource;
        }

        return newSystemResource != null;
    }

    /**
     * Create a new data stream associated with the system.
     *
     * @param dataStreamResource The data stream properties.
     * @return The new data stream or null if the creation failed.
     */
    public OSHDataStream createDataStream(DataStreamInfo dataStreamResource) throws ExecutionException, InterruptedException {
        String id = getConnectedSystemsApiClient().addDataStream(getId(), dataStreamResource).get();
        if (id == null) return null;

        var newDataStreamResource = getConnectedSystemsApiClient().getDatastreamById(id, ResourceFormat.JSON, true).get();
        if (newDataStreamResource == null) return null;

        OSHDataStream dataStream = new OSHDataStream(this, id, newDataStreamResource);
        dataStreams.add(dataStream);
        notifyDataStreamAdded(dataStream);
        return dataStream;
    }

    /**
     * Delete a data stream associated with the system.
     *
     * @param dataStream The data stream to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    public boolean deleteDataStream(OSHDataStream dataStream) throws ExecutionException, InterruptedException {
        Integer response = getConnectedSystemsApiClientExtras().deleteDataStream(dataStream.getId()).get();
        boolean success = response != null && response >= 200 && response < 300;

        if (success) {
            dataStreams.remove(dataStream);
            notifyDataStreamRemoved(dataStream);
        }

        return success;
    }

    /**
     * Create a new control stream associated with the system.
     *
     * @param controlStreamResource The control stream properties.
     * @return The new control stream or null if the creation failed.
     */
    public OSHControlStream createControlStream(CommandStreamInfo controlStreamResource) throws ExecutionException, InterruptedException {
        String id = getConnectedSystemsApiClient().addControlStream(getId(), controlStreamResource).get();
        if (id == null) return null;

        var newControlStreamResource = getConnectedSystemsApiClient().getControlStreamById(id, ResourceFormat.JSON, true).get();
        if (newControlStreamResource == null) return null;

        OSHControlStream controlStream = new OSHControlStream(this, id, newControlStreamResource);
        controlStreams.add(controlStream);
        notifyControlStreamAdded(controlStream);
        return controlStream;
    }

    /**
     * Delete a control stream associated with the system.
     *
     * @param controlStream The control stream to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    public boolean deleteControlStream(OSHControlStream controlStream) throws ExecutionException, InterruptedException {
        Integer response = getConnectedSystemsApiClientExtras().deleteControlStream(controlStream.getId()).get();
        boolean success = response != null && response >= 200 && response < 300;

        if (success) {
            controlStreams.remove(controlStream);
            notifyControlStreamRemoved(controlStream);
        }

        return success;
    }

    /**
     * Get the endpoint for the data streams of this system.
     *
     * @return The endpoint.
     */
    public String getDataStreamsEndpoint() {
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
     * Get a list of discovered data streams associated with the system.
     *
     * @return The data streams.
     */
    public List<OSHDataStream> getDataStreams() {
        return List.copyOf(dataStreams);
    }

    /**
     * Get a list of discovered control streams associated with the system.
     *
     * @return The control streams.
     */
    public List<OSHControlStream> getControlStreams() {
        return List.copyOf(controlStreams);
    }

    public ConSysApiClient getConnectedSystemsApiClient() {
        return parentNode.getConnectedSystemsApiClient();
    }

    public ConSysApiClientExtras getConnectedSystemsApiClientExtras() {
        return parentNode.getConnectedSystemsApiClientExtras();
    }

    /**
     * Add a listener for data stream notifications.
     *
     * @param listener The listener.
     */
    public void addDataStreamNotificationListener(INotificationDataStream listener) {
        dataStreamNotificationListeners.add(listener);
    }

    /**
     * Remove a listener for data stream notifications.
     *
     * @param listener The listener.
     */
    public void removeDataStreamNotificationListener(INotificationDataStream listener) {
        dataStreamNotificationListeners.remove(listener);
    }

    /**
     * Notify listeners of a new data stream.
     *
     * @param dataStream The data stream.
     */
    public void notifyDataStreamAdded(OSHDataStream dataStream) {
        for (INotificationDataStream listener : dataStreamNotificationListeners) {
            listener.onItemAdded(dataStream);
        }
    }

    /**
     * Notify listeners of a removed data stream.
     *
     * @param dataStream The data stream.
     */
    public void notifyDataStreamRemoved(OSHDataStream dataStream) {
        for (INotificationDataStream listener : dataStreamNotificationListeners) {
            listener.onItemRemoved(dataStream);
        }
    }

    /**
     * Add a listener for control stream notifications.
     *
     * @param listener The listener.
     */
    public void addControlStreamNotificationListener(INotificationControlStream listener) {
        controlStreamNotificationListeners.add(listener);
    }

    /**
     * Remove a listener for control stream notifications.
     *
     * @param listener The listener.
     */
    public void removeControlStreamNotificationListener(INotificationControlStream listener) {
        controlStreamNotificationListeners.remove(listener);
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
