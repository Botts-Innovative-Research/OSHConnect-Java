package org.sensorhub.oshconnect.net.websocket;

import lombok.Getter;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;

/**
 * Class representing a listener for a datastream.
 */
public abstract class DatastreamListener {
    /**
     * The datastream being listened to.
     */
    @Getter
    private final OSHDatastream datastream;
    private final WebSocketConnection webSocketConnection;

    protected DatastreamListener(OSHDatastream datastream, String request) {
        this.datastream = datastream;
        webSocketConnection = new WebSocketConnection(this, request);
    }

    public void onStreamUpdate(byte[] data) {
        // Default implementation does nothing
    }

    public void onStreamUpdate(String data) {
        // Default implementation does nothing
    }

    public void connect() {
        webSocketConnection.connect();
    }

    public void disconnect() {
        webSocketConnection.disconnect();
    }
}
