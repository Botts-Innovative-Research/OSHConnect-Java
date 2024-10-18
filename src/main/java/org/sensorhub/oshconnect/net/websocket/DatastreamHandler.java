package org.sensorhub.oshconnect.net.websocket;

import org.sensorhub.oshconnect.DatastreamManager;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.time.TimeExtent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lombok.Getter;

/**
 * A handler for multiple datastreams.
 * Override the {@link #onStreamUpdate(DatastreamEventArgs)} method to receive data from the datastreams.
 * Use {@link DatastreamManager#createDatastreamHandler(Consumer)} to create a new handler associated with an OSHConnect instance,
 * which will allow OSHConnect to manage the handler and shut it down when the OSHConnect instance is shut down.
 */
public abstract class DatastreamHandler implements DatastreamEventListener {
    private final List<DatastreamListener> datastreamListeners = new ArrayList<>();
    /**
     * The format of the request.
     * If null, the format will not be specified in the request, i.e. the data will be received in the default format.
     */
    @Getter
    private RequestFormat requestFormat;
    /**
     * The time period for the datastream.
     * If null, the time period will not be specified in the request, i.e. will listen to the datastream in real-time.
     */
    @Getter
    private TimeExtent timeExtent;
    /**
     * The replay speed for the datastream.
     * Only applicable for historical datastreams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * 0 or negative values will result in no data being received.
     */
    @Getter
    private double replaySpeed = 1;
    /**
     * The status of the datastream handler.
     */
    @Getter
    private StreamStatus status = StreamStatus.DISCONNECTED;

    /**
     * Creates a new datastream handler.
     * To ensure this handler is associated with an OSHConnect instance,
     * use {@link DatastreamManager#createDatastreamHandler(Consumer)} to create a new handler.
     * Doing so will allow OSHConnect to manage the handler,
     * and shut it down when the OSHConnect instance is shut down.
     */
    protected DatastreamHandler() {
    }

    /**
     * Creates a new datastream listener for the specified datastream.
     */
    private DatastreamListener createDatastreamListener(OSHDatastream datastream, DatastreamHandler handler) {
        return new DatastreamListener(datastream) {
            @Override
            public void onStreamUpdate(DatastreamEventArgs args) {
                handler.onStreamUpdate(args);
            }
        };
    }

    /**
     * Connects to all datastreams.
     */
    public void connect() {
        if (getStatus() == StreamStatus.SHUTDOWN) {
            throw new IllegalStateException("Handler has been shut down.");
        }

        removeInactiveDatastreams();
        datastreamListeners.forEach(DatastreamListener::connect);
        status = StreamStatus.CONNECTED;
    }

    /**
     * Disconnects from all datastreams.
     */
    public void disconnect() {
        datastreamListeners.forEach(DatastreamListener::disconnect);
        status = StreamStatus.DISCONNECTED;
    }

    /**
     * Shuts down the datastream handler.
     * This will disconnect from all datastreams and remove them from the handler.
     * The handler will no longer be usable after this method is called.
     */
    public void shutdown() {
        datastreamListeners.forEach(DatastreamListener::shutdown);
        datastreamListeners.clear();
        status = StreamStatus.SHUTDOWN;
    }

    /**
     * Adds a datastream to the handler.
     * Also connects to the datastream if the handler is already connected.
     * If the datastream is already in the handler, it will not be added again.
     *
     * @param datastream the datastream to add.
     */
    public void addDatastream(OSHDatastream datastream) {
        if (datastreamListeners.stream().anyMatch(l -> l.getDatastream().equals(datastream))) {
            return;
        }

        DatastreamListener listener = createDatastreamListener(datastream, this);
        listener.setRequestFormat(requestFormat);
        listener.setReplaySpeed(replaySpeed);
        listener.setTimeExtent(timeExtent);
        datastreamListeners.add(listener);

        if (status == StreamStatus.CONNECTED) {
            listener.connect();
        }
    }

    /**
     * Disconnects from the datastream and removes it from the handler.
     *
     * @param datastream the datastream to remove.
     */
    public void shutdownDatastream(OSHDatastream datastream) {
        DatastreamListener listener = datastreamListeners.stream()
                .filter(l -> l.getDatastream().equals(datastream))
                .findFirst()
                .orElse(null);
        if (listener != null) {
            listener.shutdown();
            datastreamListeners.remove(listener);
        }
    }

    /**
     * Disconnects from all datastreams and removes them from the handler.
     */
    public void shutdownAllDatastreams() {
        datastreamListeners.forEach(DatastreamListener::disconnect);
        datastreamListeners.clear();
    }

    /**
     * Removes datastreams that have been shut down.
     */
    private void removeInactiveDatastreams() {
        datastreamListeners.removeIf(listener -> listener.getStatus() == StreamStatus.SHUTDOWN);
    }

    /**
     * Get a list of datastreams in the handler.
     */
    public List<OSHDatastream> getDatastreams() {
        removeInactiveDatastreams();
        List<OSHDatastream> datastreams = new ArrayList<>();
        datastreamListeners.forEach(listener -> datastreams.add(listener.getDatastream()));
        return datastreams;
    }

    /**
     * Sets the format of the request.
     * If null, the format will not be specified in the request, i.e. the data will be received in the default format.
     * Calling this method will reconnect to the datastream if it is already connected.
     *
     * @param requestFormat the format of the request.
     *                      Set to null to remove the previously set format.
     */
    public void setRequestFormat(RequestFormat requestFormat) {
        this.requestFormat = requestFormat;
        for (DatastreamListener listener : datastreamListeners) {
            listener.setRequestFormat(requestFormat);
        }
    }

    /**
     * Sets the replay speed for the datastream.
     * Only applicable for historical datastreams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * 0 or negative values will result in no data being received.
     * Calling this method will reconnect to the datastream if it is already connected.
     *
     * @param replaySpeed the replay speed of the request.
     */
    public void setReplaySpeed(double replaySpeed) {
        this.replaySpeed = replaySpeed;
        for (DatastreamListener listener : datastreamListeners) {
            listener.setReplaySpeed(replaySpeed);
        }
    }

    /**
     * Sets the time period for the datastream.
     * If null, the time period will not be specified in the request, i.e. will listen to the datastream in real-time.
     * Calling this method will reconnect to the datastream if it is already connected.
     *
     * @param timeExtent the time period of the request.
     *                   Set to null to remove the previously set time period.
     */
    public void setTimeExtent(TimeExtent timeExtent) {
        this.timeExtent = timeExtent;
        for (DatastreamListener listener : datastreamListeners) {
            listener.setTimeExtent(timeExtent);
        }
    }
}
