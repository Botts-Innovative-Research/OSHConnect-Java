/**
 * Copyright (c) 2023. Botts Innovative Research, Inc.
 * All Rights Reserved.
 */
package org.sensorhub.oshconnect.net.websocket;

import lombok.Getter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.sensorhub.oshconnect.OSHNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an active connection to a WebSocket.
 */
public class WebSocketConnection implements WebSocketListener {
    private final StreamListener streamListener;
    private final String request;
    private final WebSocketClient client = new WebSocketClient();
    private final List<StatusListener> statusListeners = new ArrayList<>();
    @Getter
    private StreamStatus status = StreamStatus.DISCONNECTED;

    public WebSocketConnection(StreamListener streamListener, String request) {
        this.streamListener = streamListener;
        this.request = request;
    }

    public void connect() {
        if (status != StreamStatus.CONNECTED) {
            OSHNode parentNode = streamListener.getDataStream().getParentSystem().getParentNode();
            String urlString = parentNode.getWSPrefix() + request;
            ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
            clientUpgradeRequest.setHeader("Authorization", "Basic " + parentNode.getAuthorizationToken());

            try {
                client.start();
                client.connect(this, new URI(urlString), clientUpgradeRequest);
            } catch (Exception e) {
                updateStatus(StreamStatus.ERROR);
            }
        }
    }

    public void disconnect() {
        try {
            client.stop();
        } catch (Exception e) {
            updateStatus(StreamStatus.ERROR);
        }
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        streamListener.onStreamUpdate(bytes);
    }

    @Override
    public void onWebSocketText(String s) {
        // Not implemented
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        updateStatus(StreamStatus.DISCONNECTED);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        updateStatus(StreamStatus.CONNECTED);
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        updateStatus(StreamStatus.ERROR);
    }

    public void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        statusListeners.remove(listener);
    }

    private void updateStatus(StreamStatus newStatus) {
        status = newStatus;
        for (StatusListener listener : statusListeners) {
            listener.onStatusChanged(newStatus);
        }
    }
}
