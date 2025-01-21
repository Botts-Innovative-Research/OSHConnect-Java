package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.net.websocket.DataStreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DataStreamHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Manages data stream handlers and their associated data streams.
 */
public class DataStreamManager {
    /**
     * Data stream handlers added to OSHConnect.
     */
    private final Set<DataStreamHandler> dataStreamHandlers = new HashSet<>();

    /**
     * Package-private constructor, to be used by OSHConnect.
     */
    DataStreamManager() {
    }

    /**
     * Create a new data stream handler and add it to OSHConnect.
     * No need to call {@link DataStreamManager#addDataStreamHandler(DataStreamHandler)} after calling this method.
     *
     * @param onStreamUpdate The function to call when a data stream is updated.
     * @return The data stream handler.
     */
    public DataStreamHandler createDataStreamHandler(Consumer<DataStreamEventArgs> onStreamUpdate) {
        DataStreamHandler handler = new DataStreamHandler() {
            @Override
            public void onStreamUpdate(DataStreamEventArgs args) {
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
    public void addDataStreamHandler(DataStreamHandler handler) {
        dataStreamHandlers.add(handler);
    }

    /**
     * Get a list of data stream handlers associated with OSHConnect.
     *
     * @return A list of data stream handlers.
     */
    public List<DataStreamHandler> getDataStreamHandlers() {
        return new ArrayList<>(dataStreamHandlers);
    }

    /**
     * Shutdown the data stream handler and its associated data streams,
     * and remove it from OSHConnect.
     */
    public void shutdownDataStreamHandler(DataStreamHandler handler) {
        handler.shutdown();
        dataStreamHandlers.remove(handler);
    }

    /**
     * Shutdown all data stream handlers and disconnect from all data streams,
     * and remove them from OSHConnect.
     */
    public void shutdownDataStreamHandlers() {
        dataStreamHandlers.forEach(DataStreamHandler::shutdown);
        dataStreamHandlers.clear();
    }

    /**
     * Shutdown all data stream handlers.
     */
    void shutdown() {
        shutdownDataStreamHandlers();
    }
}
