package org.sensorhub.oshconnect.net.websocket;

/**
 * Interface for dataStream event listeners.
 */
public interface StreamEventListener {
    void onStreamUpdate(StreamEventArgs args);
}
