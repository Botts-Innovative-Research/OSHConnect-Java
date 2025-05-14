package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.net.websocket.StreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.StreamHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Manages data stream handlers and their associated data streams.
 */
public class StreamManager {
    /**
     * Data stream handlers added to OSHConnect.
     */
    private final Set<StreamHandler> dataStreamHandlers = new HashSet<>();

    /**
     * Package-private constructor, to be used by OSHConnect.
     */
    StreamManager() {
    }

    /**
     * Create a new data stream handler and add it to OSHConnect.
     * No need to call {@link StreamManager#addDataStreamHandler(StreamHandler)} after calling this method.
     *
     * @param onStreamUpdate The function to call when a data stream is updated.
     * @return The data stream handler.
     */
    public StreamHandler createDataStreamHandler(Consumer<StreamEventArgs> onStreamUpdate) {
        StreamHandler handler = new StreamHandler() {
            @Override
            public void onStreamUpdate(StreamEventArgs args) {
                onStreamUpdate.accept(args);
            }
        };
        addDataStreamHandler(handler);
        return handler;
    }

    /**
     * Add a data stream handler to OSHConnect.
     * This method is used to add a data stream handler created outside the OSHConnect instance.
     *
     * @param handler The data stream handler to add.
     */
    public void addDataStreamHandler(StreamHandler handler) {
        dataStreamHandlers.add(handler);
    }

    /**
     * Get a list of data stream handlers associated with OSHConnect.
     *
     * @return A list of data stream handlers.
     */
    public List<StreamHandler> getDataStreamHandlers() {
        return new ArrayList<>(dataStreamHandlers);
    }

    /**
     * Shutdown the data stream handler and its associated data streams,
     * and remove it from OSHConnect.
     */
    public void shutdownDataStreamHandler(StreamHandler handler) {
        handler.shutdown();
        dataStreamHandlers.remove(handler);
    }

    /**
     * Shutdown all data stream handlers and disconnect from all data streams,
     * and remove them from OSHConnect.
     */
    public void shutdownDataStreamHandlers() {
        dataStreamHandlers.forEach(StreamHandler::shutdown);
        dataStreamHandlers.clear();
    }

    /**
     * Shutdown all data stream handlers.
     */
    void shutdown() {
        shutdownDataStreamHandlers();
    }
}
