package org.sensorhub.oshconnect;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.Properties;
import org.sensorhub.oshconnect.net.websocket.DatastreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DatastreamHandler;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;
import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class WebSocketTest {
    long previousTimestamp;

    // This test requires a live OpenSensorHub instance to connect to.
    // Check the constants in TestConstants.java to ensure they are correct
    // and uncomment the @Disabled annotation to run the test.
    @Disabled
    @Test
    void testConnect() throws InterruptedException {
        OSHConnect oshConnect = new OSHConnect();
        OSHNode node = oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);

        //Create a new system
        Properties properties = new Properties("urn:sensor:catsensor001", null, "Cat Sensor", "A sensor that measures the number of cats in the room.", List.of("2024-12-06T18:57:51.968Z", "now"));
        OSHSystem newSystem = node.createSystem("Feature", properties);
        assertNotNull(newSystem);
        String systemId = newSystem.getId();

        // Check if the system was created
        List<OSHSystem> systems = node.getSystems();
        assertTrue(systems.stream().anyMatch(s -> s.getSystemResource().getId().equals(systemId)));
        System.out.println("System created " + systemId + ": " + newSystem.getSystemResource().getProperties().getName());

        // Discover all systems
        System.out.println("Discovering systems...");
        systems = oshConnect.discoverSystems();
        for (OSHSystem system : systems) {
            System.out.println("System " + system.getSystemResource().getId() + ": " + system.getSystemResource().getProperties().getName());
        }

        // Delete the system
        assertTrue(node.deleteSystem(newSystem));
        System.out.println("System deleted " + systemId + ": " + newSystem.getSystemResource().getProperties().getName());
        System.out.println();

        List<OSHDatastream> datastreams = oshConnect.discoverDatastreams();
        DatastreamHandler handler = oshConnect.getDatastreamManager().createDatastreamHandler(this::onStreamUpdate);

        // Add all the discovered datastreams to the handler.
        datastreams.forEach(handler::addDatastream);

        // Connect, listen for updates.
        handler.connect();
        CountDownLatch latch = new CountDownLatch(datastreams.size());
        latch.await(3, TimeUnit.SECONDS);

        // Enable time synchronization
        System.out.println("Enabling time synchronization...");
        handler.getTimeSynchronizer().enableTimeSynchronization();
        latch.await(3, TimeUnit.SECONDS);

        // Start listening for historical data instead of live data.
        System.out.println("Starting historical data...");
        Instant oneMinuteAgo = Instant.now().minusSeconds(60);
        handler.setTimeExtent(TimeExtent.beginAt(oneMinuteAgo));
        handler.setReplaySpeed(0.25);
        latch.await(3, TimeUnit.SECONDS);

        oshConnect.shutdown();
    }

    private void onStreamUpdate(DatastreamEventArgs args) {
        var datastreamId = args.getDatastream().getDatastreamResource().getId();
        var timestamp = args.getTimestamp();

        String message = String.format("onStreamUpdate: timestamp=%s datastreamId=%s", timestamp, datastreamId);
        if (timestamp < previousTimestamp) {
            message += " (out of order)";
        }
        System.out.println(message);
        previousTimestamp = timestamp;
    }
}
