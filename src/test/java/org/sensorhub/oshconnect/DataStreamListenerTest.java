package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.ObservationData;
import org.sensorhub.oshconnect.net.websocket.DataStreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.StatusListener;
import org.sensorhub.oshconnect.net.websocket.StreamStatus;
import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.sensorhub.oshconnect.tools.DataStreamTools.newDataStreamInfo;
import static org.sensorhub.oshconnect.tools.ObservationTools.newDataBlockWithData;
import static org.sensorhub.oshconnect.tools.ObservationTools.newObservationData;
import static org.sensorhub.oshconnect.tools.SystemTools.newSystem;

class DataStreamListenerTest extends TestBase {
    OSHSystem system;
    OSHDataStream dataStream;
    DataStreamManager dataStreamManager;
    Consumer<DataStreamEventArgs> emptyEvent = args -> {
        // Do nothing
    };

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
        var handler = dataStreamManager.createDataStreamHandler(emptyEvent);
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(dataStream, dataStreamListener.getDataStream());
        assertNull(dataStreamListener.getRequestFormat());
        assertNull(dataStreamListener.getTimeExtent());
        assertEquals(1, dataStreamListener.getReplaySpeed());
    }

    @Test
    void connect() {
        var handler = dataStreamManager.createDataStreamHandler(emptyEvent);
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(StreamStatus.DISCONNECTED, dataStreamListener.getStatus());

        handler.connect();
        await().until(() -> dataStreamListener.getStatus() == StreamStatus.CONNECTED);
        assertEquals(StreamStatus.CONNECTED, dataStreamListener.getStatus());
    }

    @Test
    void disconnect() {
        var handler = dataStreamManager.createDataStreamHandler(emptyEvent);
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(StreamStatus.DISCONNECTED, dataStreamListener.getStatus());

        handler.connect();
        await().until(() -> dataStreamListener.getStatus() == StreamStatus.CONNECTED);
        assertEquals(StreamStatus.CONNECTED, dataStreamListener.getStatus());

        handler.disconnect();
        assertEquals(StreamStatus.DISCONNECTED, dataStreamListener.getStatus());
    }

    @Test
    void shutdown() {
        var handler = dataStreamManager.createDataStreamHandler(emptyEvent);
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(StreamStatus.DISCONNECTED, dataStreamListener.getStatus());

        handler.shutdown();
        assertEquals(StreamStatus.SHUTDOWN, dataStreamListener.getStatus());
        assertThrows(IllegalStateException.class, handler::connect);
    }

    @Test
    void statusListener() {
        var handler = dataStreamManager.createDataStreamHandler(emptyEvent);
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);

        // Wait for the connection to be established.
        boolean[] connected = {false};
        var connectedListener = new StatusListener() {
            @Override
            public void onStatusChanged(StreamStatus newStatus) {
                if (newStatus == StreamStatus.CONNECTED) {
                    connected[0] = true;
                }
            }
        };
        dataStreamListener.addStatusListener(connectedListener);
        handler.connect();

        await().until(() -> connected[0]);
        assertTrue(connected[0]);
        dataStreamListener.removeStatusListener(connectedListener);

        // Wait for the connection to be closed.
        boolean[] disconnected = {false};
        boolean[] shutdown = {false};
        dataStreamListener.addStatusListener(newStatus -> {
            if (newStatus == StreamStatus.DISCONNECTED) {
                disconnected[0] = true;
            } else if (newStatus == StreamStatus.SHUTDOWN) {
                shutdown[0] = true;
            }
        });
        handler.shutdown();

        await().until(() -> disconnected[0] && shutdown[0]);
        assertTrue(disconnected[0]);
        assertTrue(shutdown[0]);
    }

    @Test
    void dataStreamListener_LiveData() throws InterruptedException, ExecutionException {
        boolean[] called = {false};
        DataStreamEventArgs[] receivedArgs = new DataStreamEventArgs[1];
        var handler = dataStreamManager.createDataStreamHandler(args -> {
            // This event will be called when the observation is received.
            called[0] = true;
            receivedArgs[0] = args;
        });
        var dataStreamListener = handler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(dataStream, dataStreamListener.getDataStream());
        assertEquals(1, handler.getDataStreamListeners().size());

        // Wait for the connection to be established, then push an observation.
        handler.connect();
        await().until(() -> dataStreamListener.getStatus() == StreamStatus.CONNECTED);
        Instant observationTime = Instant.now();
        pushObservation(observationTime);

        // Wait for the observation to be received.
        await().until(() -> called[0]);
        assertTrue(called[0]);
        assertNotNull(receivedArgs[0]);
        assertEquals(dataStream, receivedArgs[0].getDataStream());
        assertEquals(dataStream.getId(), receivedArgs[0].getObservation().getDataStreamId());
        assertEquals(observationTime, receivedArgs[0].getObservation().getPhenomenonTime());
    }

    @Test
    void dataStreamListener_HistoricalData() throws InterruptedException, ExecutionException {
        Instant observationStart = Instant.now().minusSeconds(2);
        Instant listenStart = Instant.now().minusSeconds(1).minusMillis(10);
        Instant listenEnd = Instant.now();

        // Push 25 observations to the data stream with a 100 ms interval, starting 2 seconds ago.
        // This will ensure that at least 1 observation falls within the listening period.
        for (int i = 0; i < 25; i++) {
            pushObservation(observationStart.plusMillis(i * 100));
        }

        boolean[] called = {false};
        DataStreamEventArgs[] receivedArgs = new DataStreamEventArgs[1];
        var handler = dataStreamManager.createDataStreamHandler(args -> {
            // This event will be called when the observation is received.
            called[0] = true;
            receivedArgs[0] = args;
        });
        handler.addDataStreamListener(dataStream);

        // Start listening for historical observations from 1 second ago to now.
        handler.setTimeExtent(TimeExtent.period(listenStart, listenEnd));
        handler.connect();

        await().until(() -> called[0]);
        assertTrue(called[0]);
        assertNotNull(receivedArgs[0]);
        Instant obsTime = receivedArgs[0].getObservation().getPhenomenonTime();
        assertTrue(obsTime.isAfter(listenStart));
        assertTrue(obsTime.isBefore(listenEnd));
    }

    private void pushObservation(Instant time) throws ExecutionException, InterruptedException {
        ObservationData observationData = newObservationData(newDataBlockWithData(), time);
        String observationID = dataStream.pushObservation(observationData);
        assertNotNull(observationID);
    }
}
