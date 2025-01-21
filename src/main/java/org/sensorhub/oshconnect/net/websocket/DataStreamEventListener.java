package org.sensorhub.oshconnect.net.websocket;

/**
 * Interface for dataStream event listeners.
 */
public interface DataStreamEventListener {
    void onStreamUpdate(DataStreamEventArgs args);
}
