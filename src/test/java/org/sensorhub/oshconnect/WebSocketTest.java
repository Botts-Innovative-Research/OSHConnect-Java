package org.sensorhub.oshconnect;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.net.websocket.DataStreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DataStreamHandler;
import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

// These tests require a live OpenSensorHub instance to connect to.
// Check the constants in TestConstants.java to ensure they are correct
// and uncomment the @Disabled annotation to run the test.
@Disabled
class WebSocketTest extends TestBase {
    long previousTimestamp = 0;

    @Test
    void testControlStreams() throws ExecutionException, InterruptedException {
        System.out.println();
        System.out.println("Control stream test");

        oshConnect.discoverSystems();
        var controlStreams = oshConnect.discoverControlStreams();

        for (var controlStream : controlStreams) {
            System.out.println("Control stream: " + controlStream.getControlStreamResource().getId());
            System.out.println(controlStream.getControlStreamResource().toJson());
        }

        assertFalse(controlStreams.isEmpty(), "No control streams found.");
    }

    @Test
    void testDatastreamListener() throws InterruptedException, ExecutionException {
        System.out.println();
        System.out.println("Datastream listener test");

        oshConnect.discoverSystems();
        List<OSHDataStream> datastreams = oshConnect.discoverDataStreams();
        assertFalse(datastreams.isEmpty(), "No datastreams found.");

        DataStreamHandler handler = oshConnect.getDataStreamManager().createDataStreamHandler(this::onStreamUpdate);

        // Add all the discovered datastreams to the handler.
        for (OSHDataStream datastream : datastreams) {
            handler.addDatastream(datastream);
        }

        // Connect, listen for updates.
        handler.connect();
        CountDownLatch latch = new CountDownLatch(datastreams.size());
        latch.await(1, TimeUnit.SECONDS);

        // Enable time synchronization
        System.out.println("Enabling time synchronization...");
        handler.getTimeSynchronizer().enableTimeSynchronization();
        latch.await(2, TimeUnit.SECONDS);

        // Start listening for historical data instead of live data.
        System.out.println("Starting historical data...");
        Instant oneMinuteAgo = Instant.now().minusSeconds(60);
        handler.setTimeExtent(TimeExtent.beginAt(oneMinuteAgo));
        handler.setReplaySpeed(0.25);
        latch.await(1, TimeUnit.SECONDS);
    }

    private void onStreamUpdate(DataStreamEventArgs args) {
        var datastreamId = args.getDataStream().getId();
        var timestamp = args.getTimestamp();

        String message = String.format("onStreamUpdate: timestamp=%s datastreamId=%s", timestamp, datastreamId);
        if (timestamp < previousTimestamp) {
            message += " (out of order)";
        }
        System.out.println(message);
        previousTimestamp = timestamp;
    }
}
