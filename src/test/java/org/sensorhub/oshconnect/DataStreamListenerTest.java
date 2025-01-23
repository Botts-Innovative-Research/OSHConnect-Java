package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.ObservationData;
import org.sensorhub.oshconnect.net.websocket.StatusListener;
import org.sensorhub.oshconnect.net.websocket.StreamStatus;
import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.sensorhub.oshconnect.tools.DataStreamTools.newDataStreamInfo;
import static org.sensorhub.oshconnect.tools.ObservationTools.newDataBlockWithData;
import static org.sensorhub.oshconnect.tools.ObservationTools.newObservationData;
import static org.sensorhub.oshconnect.tools.SystemTools.newSystem;

class DataStreamListenerTest extends TestBase {
    OSHSystem system;
    OSHDataStream dataStream;
    DataStreamManager dataStreamManager;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException {
        dataStreamManager = oshConnect.getDataStreamManager();
        system = node.createSystem(newSystem());
        assertNotNull(system);
        dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);
    }

    @Test
    void dataStreamListener() {
        var handler = dataStreamManager.createDataStreamHandler(args -> {
        });
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(dataStream, dataStreamListener.getDataStream());
        assertNull(dataStreamListener.getRequestFormat());
        assertNull(dataStreamListener.getTimeExtent());
        assertEquals(1, dataStreamListener.getReplaySpeed());
    }

    @Test
    void connect() throws ExecutionException, InterruptedException, TimeoutException {
        var handler = dataStreamManager.createDataStreamHandler(args -> {
        });
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(StreamStatus.DISCONNECTED, dataStreamListener.getStatus());

        // Wait for the connection to be established.
        CompletableFuture<Void> cf = new CompletableFuture<>();
        boolean[] connected = {false};
        dataStreamListener.addStatusListener(newStatus -> {
            if (newStatus == StreamStatus.CONNECTED) {
                connected[0] = true;
                cf.complete(null);
            }
        });
        handler.connect();

        cf.get(500, TimeUnit.MILLISECONDS);
        assertTrue(connected[0]);
    }

    @Test
    void disconnect() throws ExecutionException, InterruptedException, TimeoutException {
        var handler = dataStreamManager.createDataStreamHandler(args -> {
        });
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(StreamStatus.DISCONNECTED, dataStreamListener.getStatus());

        // Wait for the connection to be established.
        CompletableFuture<Void> cf1 = new CompletableFuture<>();
        boolean[] connected = {false};
        var connectedListener = new StatusListener() {
            @Override
            public void onStatusChanged(StreamStatus newStatus) {
                if (newStatus == StreamStatus.CONNECTED) {
                    connected[0] = true;
                    cf1.complete(null);
                }
            }
        };
        dataStreamListener.addStatusListener(connectedListener);
        handler.connect();

        cf1.get(500, TimeUnit.MILLISECONDS);
        assertTrue(connected[0]);
        dataStreamListener.removeStatusListener(connectedListener);

        // Wait for the connection to be closed.
        CompletableFuture<Void> cf2 = new CompletableFuture<>();
        boolean[] disconnected = {false};
        dataStreamListener.addStatusListener(newStatus -> {
            if (newStatus == StreamStatus.DISCONNECTED) {
                disconnected[0] = true;
                cf2.complete(null);
            }
        });
        handler.disconnect();

        cf2.get(500, TimeUnit.MILLISECONDS);
        assertTrue(disconnected[0]);
    }

    @Test
    void shutdown() throws ExecutionException, InterruptedException, TimeoutException {
        var handler = dataStreamManager.createDataStreamHandler(args -> {
        });
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(StreamStatus.DISCONNECTED, dataStreamListener.getStatus());

        // Wait for the connection to be established.
        CompletableFuture<Void> cf1 = new CompletableFuture<>();
        boolean[] connected = {false};
        var connectedListener = new StatusListener() {
            @Override
            public void onStatusChanged(StreamStatus newStatus) {
                if (newStatus == StreamStatus.CONNECTED) {
                    connected[0] = true;
                    cf1.complete(null);
                }
            }
        };
        dataStreamListener.addStatusListener(connectedListener);
        handler.connect();

        cf1.get(500, TimeUnit.MILLISECONDS);
        assertTrue(connected[0]);
        dataStreamListener.removeStatusListener(connectedListener);

        // Wait for the connection to be closed.
        CompletableFuture<Void> cf2 = new CompletableFuture<>();
        boolean[] disconnected = {false};
        boolean[] shutdown = {false};
        dataStreamListener.addStatusListener(newStatus -> {
            if (newStatus == StreamStatus.DISCONNECTED) {
                disconnected[0] = true;
            } else if (newStatus == StreamStatus.SHUTDOWN) {
                shutdown[0] = true;
                cf2.complete(null);
            }
        });
        handler.shutdown();

        cf2.get(500, TimeUnit.MILLISECONDS);
        assertTrue(disconnected[0]);
        assertTrue(shutdown[0]);
    }

    @Test
    void dataStreamListener_LiveData() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        boolean[] called = {false};
        var handler = dataStreamManager.createDataStreamHandler(args -> {
            // This event will be called when the observation is received.
            called[0] = true;
            cf.complete(null);
        });
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(dataStream, dataStreamListener.getDataStream());
        assertEquals(1, handler.getDataStreamListeners().size());

        // Wait for the connection to be established, then push an observation.
        var statusListener = new StatusListener() {
            @Override
            public void onStatusChanged(StreamStatus newStatus) {
                System.out.println("Status: " + newStatus);
                if (newStatus == StreamStatus.CONNECTED) {
                    try {
                        ObservationData observationData = newObservationData(newDataBlockWithData());
                        String observationID = dataStream.pushObservation(observationData);
                        assertNotNull(observationID);
                    } catch (ExecutionException | InterruptedException e) {
                        fail(e);
                    }
                }
            }
        };
        dataStreamListener.addStatusListener(statusListener);
        handler.connect();

        // Wait for the observation to be received.
        cf.get(500, TimeUnit.MILLISECONDS);
        assertTrue(called[0]);
    }

    @Test
    void dataStreamListener_HistoricalData() throws InterruptedException, ExecutionException, TimeoutException {
        Instant observationStart = Instant.now().minusSeconds(2);
        Instant listenStart = Instant.now().minusSeconds(1);
        Instant listenEnd = Instant.now();

        // Push 25 observations to the data stream with a 100 ms interval, starting 2 seconds ago.
        // This will ensure that at least 1 observation falls within the listening period.
        for (int i = 0; i < 25; i++) {
            ObservationData observationData = newObservationData(newDataBlockWithData(), observationStart.plusMillis(i * 100));
            String observationID = dataStream.pushObservation(observationData);
            assertNotNull(observationID);
        }

        CompletableFuture<Void> cf = new CompletableFuture<>();
        boolean[] called = {false};
        var handler = dataStreamManager.createDataStreamHandler(args -> {
            // This event will be called when the observation is received.
            called[0] = true;
            cf.complete(null);

            Instant obsTime = args.getObservation().getPhenomenonTime();
            assertTrue(obsTime.isAfter(listenStart));
            assertTrue(obsTime.isBefore(listenEnd));
        });
        handler.addDataStreamListener(dataStream);

        // Start listening for historical observations from 1 second ago to now.
        handler.setTimeExtent(TimeExtent.period(listenStart, listenEnd));
        handler.connect();

        cf.get(500, TimeUnit.MILLISECONDS);
        assertTrue(called[0]);
    }
}
