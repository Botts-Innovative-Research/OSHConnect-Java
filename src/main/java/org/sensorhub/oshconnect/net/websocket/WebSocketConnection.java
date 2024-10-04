/**
 * Copyright (c) 2023. Botts Innovative Research, Inc.
 * All Rights Reserved.
 */
package org.sensorhub.oshconnect.net.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.net.URI;

/**
 * Class representing an active connection to a WebSocket.
 */
public class WebSocketConnection implements WebSocketListener {
    private final DatastreamListener streamListener;
    private final String request;
    private final WebSocketClient client = new WebSocketClient();
    private StreamStatus status = StreamStatus.DISCONNECTED;

    public WebSocketConnection(DatastreamListener streamListener, String request) {
        this.streamListener = streamListener;
        this.request = request;
    }

    public void connect() {
        if (status != StreamStatus.CONNECTED) {
            OSHNode parentNode = streamListener.getDatastream().getParentSystem().getParentNode();

            // Build the request on the WebSocket protocol
            String urlString = parentNode.getWSPrefix() + request;

            // Setup and upgrade request to pass along credentials
            ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();

            // Add the credentials to use
            clientUpgradeRequest.setHeader("Authorization", "Basic " + parentNode.getAuthorizationToken());

            try {
                // Start the client
                client.start();

                // Establish the connection
                client.connect(this, new URI(urlString), clientUpgradeRequest);
            } catch (Exception e) {
                status = StreamStatus.ERROR;
            }
        }
    }

    public void disconnect() {
        try {
            client.stop();
        } catch (Exception e) {
            status = StreamStatus.ERROR;
        }
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        streamListener.onStreamUpdate(bytes);
    }

    @Override
    public void onWebSocketText(String s) {
        streamListener.onStreamUpdate(s);
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        status = StreamStatus.DISCONNECTED;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        status = StreamStatus.CONNECTED;
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        status = StreamStatus.ERROR;
    }
}
