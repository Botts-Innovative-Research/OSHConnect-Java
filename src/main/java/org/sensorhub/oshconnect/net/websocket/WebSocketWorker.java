/**
 * Copyright (c) 2023. Botts Innovative Research, Inc.
 * All Rights Reserved.
 */

package org.sensorhub.oshconnect.net.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.sensorhub.oshconnect.net.Protocol;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class responsible for establishing and executing the lifecycle management of
 * web sockets as data sources for visualizations.
 */
public class WebSocketWorker implements WebSocketListener, Runnable {
    /**
     * Flag to control execution of this <code>Runnable</code>, when set to
     * false will indicate the loop in the <code>run</code> routine is to
     * terminate, websocket connection is closed, and resources cleaned up
     */
    private final AtomicBoolean doWork = new AtomicBoolean(true);

    /**
     * Flag used to indicate if this worker is currently connected to the data
     * source.
     */
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    /**
     * The datastream object being updated by this worker
     */
    private final OSHDatastream datastream;

    /**
     * The request to be made when the worker is executed.
     */
    private final String request;

    /**
     * @param request The request to be made when the worker is executed.
     */
    public WebSocketWorker(OSHDatastream datastream, String request) {
        this.datastream = datastream;
        this.request = request;
    }

    /**
     * Command this worker to start if it is currently not connected, otherwise does nothing.
     */
    public void connect() {
        if (!isConnected()) {
            doWork.set(true);
            Thread workerThread = new Thread(this);
            workerThread.start();
        }
    }

    /**
     * Checks if the worker is currently connected
     *
     * @return The state of the <code>isConnected</code> flag
     */
    public boolean isConnected() {
        return isConnected.get();
    }

    /**
     * Sets the <code>doWork</code> flag to false and notifies the worker thread
     */
    public void disconnect() {
        synchronized (doWork) {
            doWork.set(false);
            doWork.notifyAll();
        }

        // Reset isConnected flag
        isConnected.set(false);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        System.out.println("onWebSocketBinary: binary message received");
        System.out.println("onWebSocketBinary: payload = " + (new String(payload)));
    }

    @Override
    public void onWebSocketText(String message) {
        System.out.println("onWebSocketText: text message received");
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        System.out.println("onWebSocketClose: statusCode = " + statusCode + " | reason = " + reason);
        disconnect();
    }

    @Override
    public void onWebSocketConnect(Session session) {
        System.out.println("onWebSocketConnect: connected");
        isConnected.set(true);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        System.out.println("onWebSocketError: " + cause.getMessage());
        disconnect();
    }

    @Override
    public void run() {
        // Get the parent node, each visualization is associated with an instance of OpenSensorHub
        // from which it can request samples to update the visualization
        OSHNode parentOSHNode = datastream.getParentSystem().getParentNode();

        // Get the protocol to use, in this case a WebSocket protocol, either secure or unsecured
        String protocol = (parentOSHNode.isSecure()) ? Protocol.WSS.getPrefix() : Protocol.WS.getPrefix();

        // Build the request on the given protocol
        String urlString = protocol + parentOSHNode.getApiEndpoint() + request;

        System.out.println("url: " + urlString);

        // Create an instance of the web socket client to manage the connection
        WebSocketClient client = new WebSocketClient();

        // Setup and upgrade request to pass along credentials
        ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();

        // Add the credentials to use
        clientUpgradeRequest.setHeader("Authorization", "Basic " + parentOSHNode.getAuthorizationToken());

        try {
            // Start the client
            client.start();

            // Establish the connection
            client.connect(this, new URI(urlString), clientUpgradeRequest);

            synchronized (doWork) {
                // While required to maintain connection
                while (doWork.get()) {
                    // Wait for state to change or be commanded to change
                    doWork.wait();
                }
            }
        } catch (Exception ex) {
            System.out.println("run: " + ex.getMessage());
        } finally {
            try {
                // Stop the client, thus closing the connection
                client.stop();
            } catch (Exception ex) {
                System.out.println("run: " + ex.getMessage());
            }

            // Reset doWork flag
            doWork.set(false);
        }
    }
}
