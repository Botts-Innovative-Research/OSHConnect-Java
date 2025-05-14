package org.sensorhub.oshconnect.net.websocket;

/**
 * Interface for listening to status changes of a WebSocket connection.
 */
public interface StatusListener {
    void onStatusChanged(StreamStatus newStatus);
}
