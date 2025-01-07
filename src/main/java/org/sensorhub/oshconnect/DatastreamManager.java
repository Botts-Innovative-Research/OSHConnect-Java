package org.sensorhub.oshconnect;

import org.sensorhub.oshconnect.net.websocket.DatastreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DatastreamHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Manages datastream handlers and their associated datastreams.
 */
public class DatastreamManager {
    /**
     * Datastream handlers added to OSHConnect.
     */
    private final Set<DatastreamHandler> datastreamHandlers = new HashSet<>();

    /**
     * Package-private constructor, to be used by OSHConnect.
     */
    DatastreamManager() {
    }

    /**
     * Create a new datastream handler and add it to OSHConnect.
     * No need to call {@link DatastreamManager#addDatastreamHandler(DatastreamHandler)} after calling this method.
     *
     * @param onStreamUpdate The function to call when a datastream is updated.
     * @return The datastream handler.
     */
    public DatastreamHandler createDatastreamHandler(Consumer<DatastreamEventArgs> onStreamUpdate) {
        DatastreamHandler handler = new DatastreamHandler() {
            @Override
            public void onStreamUpdate(DatastreamEventArgs args) {
                onStreamUpdate.accept(args);
            }
        };
        addDatastreamHandler(handler);
        return handler;
    }

    /**
     * Add a datastream handler to OSHConnect.
     * This method is used to add a datastream handler created outside the OSHConnect instance.
     *
     * @param handler The datastream handler to add.
     */
    public void addDatastreamHandler(DatastreamHandler handler) {
        datastreamHandlers.add(handler);
    }

    /**
     * Get a list of datastream handlers associated with OSHConnect.
     *
     * @return A list of datastream handlers.
     */
    public List<DatastreamHandler> getDatastreamHandlers() {
        return new ArrayList<>(datastreamHandlers);
    }

    /**
     * Shutdown the datastream handler and its associated datastreams,
     * and remove it from OSHConnect.
     */
    public void shutdownDatastreamHandler(DatastreamHandler handler) {
        handler.shutdown();
        datastreamHandlers.remove(handler);
    }

    /**
     * Shutdown all datastream handlers and disconnect from all datastreams,
     * and remove them from OSHConnect.
     */
    public void shutdownDatastreamHandlers() {
        datastreamHandlers.forEach(DatastreamHandler::shutdown);
        datastreamHandlers.clear();
    }

    /**
     * Shutdown all datastream handlers.
     */
    void shutdown() {
        shutdownDatastreamHandlers();
    }
}
