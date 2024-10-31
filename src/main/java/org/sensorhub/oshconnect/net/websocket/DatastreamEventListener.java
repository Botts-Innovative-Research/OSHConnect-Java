package org.sensorhub.oshconnect.net.websocket;

/**
 * Interface for datastream event listeners.
 */
public interface DatastreamEventListener {
    void onStreamUpdate(DatastreamEventArgs args);
}
