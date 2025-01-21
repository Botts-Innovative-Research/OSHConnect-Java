package org.sensorhub.oshconnect.net.websocket;

import lombok.Getter;
import org.sensorhub.oshconnect.DataStreamManager;
import org.sensorhub.oshconnect.OSHDataStream;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.time.TimeSynchronizer;
import org.vast.util.TimeExtent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A handler for multiple data streams.
 * Override the {@link #onStreamUpdate(DataStreamEventArgs)} method to receive data from the data streams.
 * Use {@link DataStreamManager#createDataStreamHandler(Consumer)} to create a new handler associated with an OSHConnect instance,
 * which will allow OSHConnect to manage the handler and shut it down when the OSHConnect instance is shut down.
 */
public abstract class DataStreamHandler implements DataStreamEventListener {
    private final List<DataStreamListener> dataStreamListeners = new ArrayList<>();
    @Getter
    private final TimeSynchronizer<DataStreamEventArgs> timeSynchronizer;

    /**
     * The format of the request.
     * If null, the format will not be specified in the request, i.e., the data will be received in the default format.
     */
    @Getter
    private RequestFormat requestFormat;
    /**
     * The time period for the data stream.
     * If null, the time period will not be specified in the request, i.e., will listen to the data stream in real-time.
     */
    @Getter
    private TimeExtent timeExtent;
    /**
     * The replay speed for the data stream.
     * Only applicable for historical data streams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * 0 or negative values will result in no data being received.
     */
    @Getter
    private double replaySpeed = 1;
    /**
     * The status of the data stream handler.
     */
    @Getter
    private StreamStatus status = StreamStatus.DISCONNECTED;

    /**
     * Creates a new data stream handler.
     * To ensure this handler is associated with an OSHConnect instance,
     * use {@link DataStreamManager#createDataStreamHandler(Consumer)} to create a new handler.
     * Doing so will allow OSHConnect to manage the handler,
     * and shut it down when the OSHConnect instance is shut down.
     */
    protected DataStreamHandler() {
        this.timeSynchronizer = new TimeSynchronizer<>(this::onStreamUpdate);
    }

    /**
     * Connects to all data streams.
     */
    public void connect() {
        if (getStatus() == StreamStatus.SHUTDOWN) {
            throw new IllegalStateException("Handler has been shut down.");
        }

        for (DataStreamListener listener : dataStreamListeners) {
            if (listener.getStatus() != StreamStatus.SHUTDOWN) {
                listener.connect();
            }
        }
        status = StreamStatus.CONNECTED;
    }

    /**
     * Disconnects from all data streams.
     */
    public void disconnect() {
        dataStreamListeners.forEach(DataStreamListener::disconnect);
        status = StreamStatus.DISCONNECTED;
    }

    /**
     * Shuts down the data stream handler.
     * This will disconnect from all data streams and remove them from the handler.
     * The handler will no longer be usable after this method is called.
     */
    public void shutdown() {
        shutdownAllDatastreams();
        status = StreamStatus.SHUTDOWN;
    }

    /**
     * Adds a data stream to the handler.
     * Also connects to the data stream if the handler is already connected.
     * If the data stream is already in the handler, it will not be added again.
     *
     * @param dataStream the data stream to add.
     */
    public void addDatastream(OSHDataStream dataStream) {
        if (dataStreamListeners.stream().anyMatch(l -> l.getDataStream().equals(dataStream))) {
            return;
        }

        DataStreamListener listener = new DataStreamListener(dataStream) {
            @Override
            public void onStreamUpdate(DataStreamEventArgs args) {
                timeSynchronizer.addEvent(args.getTimestamp(), args);
            }
        };

        listener.setRequestFormat(requestFormat);
        listener.setReplaySpeed(replaySpeed);
        listener.setTimeExtent(timeExtent);
        dataStreamListeners.add(listener);

        if (status == StreamStatus.CONNECTED) {
            listener.connect();
        }
    }

    /**
     * Disconnects from the data stream and removes it from the handler.
     *
     * @param dataStream the data stream to remove.
     */
    public void shutdownDatastream(OSHDataStream dataStream) {
        DataStreamListener listener = dataStreamListeners.stream()
                .filter(l -> l.getDataStream().equals(dataStream))
                .findFirst()
                .orElse(null);
        if (listener != null) {
            listener.shutdown();
            dataStreamListeners.remove(listener);
        }
    }

    /**
     * Shuts down all data streams and removes them from the handler.
     */
    public void shutdownAllDatastreams() {
        dataStreamListeners.forEach(DataStreamListener::disconnect);
        dataStreamListeners.clear();
    }

    /**
     * Get a list of data streams in the handler.
     */
    public List<OSHDataStream> getDatastreams() {
        List<OSHDataStream> dataStreams = new ArrayList<>();
        dataStreamListeners.forEach(listener -> dataStreams.add(listener.getDataStream()));
        return dataStreams;
    }

    /**
     * Sets the format of the request.
     * If null, the format will not be specified in the request, i.e., the data will be received in the default format.
     * Calling this method will reconnect to the data stream if it is already connected.
     *
     * @param requestFormat the format of the request.
     *                      Set to null to remove the previously set format.
     */
    public void setRequestFormat(RequestFormat requestFormat) {
        this.requestFormat = requestFormat;
        for (DataStreamListener listener : dataStreamListeners) {
            listener.setRequestFormat(requestFormat);
        }
    }

    /**
     * Sets the replay speed for the data stream.
     * Only applicable for historical data streams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * 0 or negative values will result in no data being received.
     * Calling this method will reconnect to the data stream if it is already connected.
     *
     * @param replaySpeed the replay speed of the request.
     */
    public void setReplaySpeed(double replaySpeed) {
        this.replaySpeed = replaySpeed;
        for (DataStreamListener listener : dataStreamListeners) {
            listener.setReplaySpeed(replaySpeed);
        }
    }

    /**
     * Sets the time period for the data stream.
     * If null, the time period will not be specified in the request, i.e., will listen to the data stream in real-time.
     * Calling this method will reconnect to the data stream if it is already connected.
     *
     * @param timeExtent the time period of the request.
     *                   Set to null to remove the previously set time period.
     */
    public void setTimeExtent(TimeExtent timeExtent) {
        this.timeExtent = timeExtent;
        for (DataStreamListener listener : dataStreamListeners) {
            listener.setTimeExtent(timeExtent);
        }
    }
}
