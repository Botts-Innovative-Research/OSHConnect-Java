package org.sensorhub.oshconnect.net.websocket;

import org.sensorhub.oshconnect.OSHStream;
import org.sensorhub.oshconnect.StreamManager;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.time.TimeSynchronizer;
import org.vast.util.TimeExtent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A handler for multiple data streams.
 * Override the {@link #onStreamUpdate(StreamEventArgs)} method to receive data from the data streams.
 * Use {@link StreamManager#createDataStreamHandler(Consumer)} to create a new handler associated with an OSHConnect instance,
 * which will allow OSHConnect to manage the handler and shut it down when the OSHConnect instance is shut down.
 */
public abstract class StreamHandler implements StreamEventListener {
    private final List<StreamListener> dataStreamListeners = new ArrayList<>();
    private final TimeSynchronizer<StreamEventArgs> timeSynchronizer;

    /**
     * The format of the request.
     * If null, the format will not be specified in the request, i.e., the data will be received in the default format.
     */
    private RequestFormat requestFormat;
    /**
     * The time period for the data stream.
     * If null, the time period will not be specified in the request, i.e., will listen to the data stream in real-time.
     */
    private TimeExtent timeExtent;
    /**
     * The replay speed for the data stream.
     * Only applicable for historical data streams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * Zero or negative values will result in no data being received.
     */
    private double replaySpeed = 1;
    /**
     * The status of the data stream handler.
     */
    private StreamStatus status = StreamStatus.DISCONNECTED;

    /**
     * Creates a new data stream handler.
     * To ensure this handler is associated with an OSHConnect instance,
     * use {@link StreamManager#createDataStreamHandler(Consumer)} to create a new handler.
     * Doing so will allow OSHConnect to manage the handler,
     * and shut it down when the OSHConnect instance is shut down.
     */
    protected StreamHandler() {
        this.timeSynchronizer = new TimeSynchronizer<>(this::onStreamUpdate);
    }

    /**
     * Connects to all data streams.
     */
    public void connect() {
        if (getStatus() == StreamStatus.SHUTDOWN) {
            throw new IllegalStateException("Handler has been shut down.");
        }

        for (StreamListener listener : dataStreamListeners) {
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
        dataStreamListeners.forEach(StreamListener::disconnect);
        status = StreamStatus.DISCONNECTED;
    }

    /**
     * Shuts down the data stream handler.
     * This will disconnect from all data streams and remove them from the handler.
     * The handler will no longer be usable after this method is called.
     */
    public void shutdown() {
        shutdownAllDataStreamListeners();
        status = StreamStatus.SHUTDOWN;
    }

    /**
     * Adds a data stream to the handler.
     * Also connects to the data stream if the handler is already connected.
     * If the data stream is already in the handler, it will not be added again.
     *
     * @param dataStream the data stream to add.
     */
    public StreamListener addDataStreamListener(OSHStream dataStream) {
        if (dataStream == null) {
            throw new IllegalArgumentException("Data stream cannot be null.");
        }

        for (StreamListener listener : dataStreamListeners) {
            if (listener.getDataStream().equals(dataStream)) {
                return listener;
            }
        }

        StreamListener listener = new StreamListener(dataStream) {
            @Override
            public void onStreamUpdate(StreamEventArgs args) {
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

        return listener;
    }

    /**
     * Disconnects from the data stream and removes it from the handler.
     *
     * @param dataStream the data stream to remove.
     * @return true if the DataStreamListener was shut down and removed, false if the data stream was not in the handler.
     */
    public boolean shutdownDataStreamListener(OSHStream dataStream) {
        if (dataStream == null) return false;

        StreamListener listener = dataStreamListeners.stream()
                .filter(l -> l.getDataStream().equals(dataStream))
                .findFirst()
                .orElse(null);
        return shutdownDataStreamListener(listener);
    }

    /**
     * Disconnects from the data stream and removes it from the handler.
     * If the DataStreamListener does not belong to this handler, this method will do nothing.
     *
     * @param listener the DataStreamListener to remove.
     * @return true if the DataStreamListener was removed, false if the DataStreamListener was not in the handler.
     */
    public boolean shutdownDataStreamListener(StreamListener listener) {
        if (listener == null) return false;

        boolean removed = dataStreamListeners.remove(listener);
        if (removed) {
            listener.shutdown();
        }
        return removed;
    }

    /**
     * Shuts down all data streams and removes them from the handler.
     */
    public void shutdownAllDataStreamListeners() {
        dataStreamListeners.forEach(StreamListener::shutdown);
        dataStreamListeners.clear();
    }

    /**
     * Get a list of data streams listeners in the handler.
     */
    public List<StreamListener> getDataStreamListeners() {
        return new ArrayList<>(dataStreamListeners);
    }

    public TimeSynchronizer<StreamEventArgs> getTimeSynchronizer() {
        return timeSynchronizer;
    }

    /**
     * The format of the request.
     * If null, the format will not be specified in the request, i.e., the data will be received in the default format.
     */
    public RequestFormat getRequestFormat() {
        return requestFormat;
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
        for (StreamListener listener : dataStreamListeners) {
            listener.setRequestFormat(requestFormat);
        }
    }

    /**
     * The time period for the data stream.
     * If null, the time period will not be specified in the request, i.e., will listen to the data stream in real-time.
     */
    public TimeExtent getTimeExtent() {
        return timeExtent;
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
        for (StreamListener listener : dataStreamListeners) {
            listener.setTimeExtent(timeExtent);
        }
    }

    /**
     * The replay speed for the data stream.
     * Only applicable for historical data streams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * Zero or negative values will result in no data being received.
     */
    public double getReplaySpeed() {
        return replaySpeed;
    }

    /**
     * Sets the replay speed for the data stream.
     * Only applicable for historical data streams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * Zero or negative values will result in no data being received.
     * Calling this method will reconnect to the data stream if it is already connected.
     *
     * @param replaySpeed the replay speed of the request.
     */
    public void setReplaySpeed(double replaySpeed) {
        this.replaySpeed = replaySpeed;
        for (StreamListener listener : dataStreamListeners) {
            listener.setReplaySpeed(replaySpeed);
        }
    }

    /**
     * The status of the data stream handler.
     */
    public StreamStatus getStatus() {
        return status;
    }
}
