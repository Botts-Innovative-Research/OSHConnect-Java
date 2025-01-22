package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.ObservationData;
import org.sensorhub.oshconnect.net.websocket.DataStreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DataStreamHandler;
import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;
import static org.sensorhub.oshconnect.tools.DataStreamTools.newDataStreamInfo;
import static org.sensorhub.oshconnect.tools.ObservationTools.newDataBlockWithData;
import static org.sensorhub.oshconnect.tools.ObservationTools.newObservationData;
import static org.sensorhub.oshconnect.tools.SystemTools.newSystem;

class DataStreamManagerTest extends TestBase {
    private DataStreamManager dataStreamManager;

    @BeforeEach
    void setup() {
        dataStreamManager = oshConnect.getDataStreamManager();
    }

    @Test
    void createDataStreamHandler() {
        dataStreamManager.createDataStreamHandler(args -> {
            // Do nothing
        });
        assertEquals(1, dataStreamManager.getDataStreamHandlers().size());
    }

    @Test
    void addDataStreamHandler() {
        DataStreamHandler handler = new DataStreamHandler() {
            @Override
            public void onStreamUpdate(DataStreamEventArgs args) {
                // Do nothing
            }
        };
        assertEquals(0, dataStreamManager.getDataStreamHandlers().size());
        dataStreamManager.addDataStreamHandler(handler);
        assertEquals(1, dataStreamManager.getDataStreamHandlers().size());
    }

    @Test
    void shutdownDataStreamHandler() {
        DataStreamHandler handler = dataStreamManager.createDataStreamHandler(args -> {
            // Do nothing
        });

        assertEquals(1, dataStreamManager.getDataStreamHandlers().size());
        dataStreamManager.shutdownDataStreamHandler(handler);
        assertEquals(0, dataStreamManager.getDataStreamHandlers().size());

        assertThrows(IllegalStateException.class, handler::connect);
    }

    @Test
    void shutdownDataStreamHandlers() {
        dataStreamManager.createDataStreamHandler(args -> {
            // Do nothing
        });
        dataStreamManager.createDataStreamHandler(args -> {
            // Do nothing
        });

        assertEquals(2, dataStreamManager.getDataStreamHandlers().size());
        dataStreamManager.shutdownDataStreamHandlers();
        assertEquals(0, dataStreamManager.getDataStreamHandlers().size());
    }

    @Test
    void datastreamListener() throws InterruptedException, ExecutionException, TimeoutException {
        OSHSystem system = node.createSystem(newSystem());
        assertNotNull(system);
        OSHDataStream dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);

        CompletableFuture<Void> cf = new CompletableFuture<>();
        boolean[] called = {false};
        var handler = dataStreamManager.createDataStreamHandler(args -> {
            called[0] = true;
            cf.complete(null);
        });
        handler.addDatastream(dataStream);
        handler.connect();

        // Start a thread and post an observation to the datastream.
        new Thread(() -> {
            try {
                sleep(25);
                ObservationData observationData = newObservationData(newDataBlockWithData());
                String observationID = dataStream.pushObservation(observationData);
                assertNotNull(observationID);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).start();

        cf.get(100, TimeUnit.MILLISECONDS);
        assertTrue(called[0]);
    }

    @Test
    void datastreamListenerHistorical() throws InterruptedException, ExecutionException, TimeoutException {
        OSHSystem system = node.createSystem(newSystem());
        assertNotNull(system);
        OSHDataStream dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);

        Instant observationStart = Instant.now().minusSeconds(2);
        Instant listenStart = Instant.now().minusSeconds(1);
        Instant listenEnd = Instant.now();

        // Push 25 observations to the datastream with a 100 ms interval, starting 2 seconds ago.
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

            // Ensure that the observation time is within the listening period.
            String obsTime = args.getObservation().getPhenomenonTime();
            Instant obsInstant = Instant.parse(obsTime);
            assertTrue(obsInstant.isAfter(listenStart));
            assertTrue(obsInstant.isBefore(listenEnd));
        });
        handler.addDatastream(dataStream);

        // Start listening for historical observations from 1 second ago to now.
        handler.setTimeExtent(TimeExtent.period(listenStart, listenEnd));
        handler.connect();

        cf.get(500, TimeUnit.MILLISECONDS);
        handler.disconnect();
        assertTrue(called[0]);
    }
}
