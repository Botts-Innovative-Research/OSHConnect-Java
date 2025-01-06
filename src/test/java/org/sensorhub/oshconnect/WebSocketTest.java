package org.sensorhub.oshconnect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.Properties;
import org.sensorhub.oshconnect.datamodels.SystemResource;
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

// These tests requires a live OpenSensorHub instance to connect to.
// Check the constants in TestConstants.java to ensure they are correct
// and uncomment the @Disabled annotation to run the test.
@Disabled
class WebSocketTest {
    long previousTimestamp;
    private OSHConnect oshConnect;
    private OSHNode node;

    @BeforeEach
    void setUp() {
        oshConnect = new OSHConnect();
        node = oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
    }

    @AfterEach
    void tearDown() {
        oshConnect.shutdown();
    }

    @Test
    void testControlStreams() {
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
    void testAddUpdateDeleteSystem() {
        System.out.println();
        System.out.println("Add, update, and delete system test");

        //Create a new system
        Properties properties = new Properties(null, "urn:sensor:cat_sensor_001", "Cat Sensor", "A sensor that measures the number of cats in the room.", null, List.of("2024-12-06T18:57:51.968Z", "now"), null);
        OSHSystem newSystem = node.createSystem(properties);
        assertNotNull(newSystem);
        String systemId = newSystem.getId();

        // Check if the system was created
        List<OSHSystem> systems = node.getSystems();
        assertTrue(systems.stream().anyMatch(s -> s.getSystemResource().getId().equals(systemId)));
        System.out.println("System created " + systemId + ": " + newSystem.getSystemResource().getProperties().getName());

        // Discover all systems
        systems = oshConnect.discoverSystems();
        for (OSHSystem system : systems) {
            if (system.getSystemResource().getId().equals(systemId)) {
                System.out.println("System discovered " + systemId + ": " + system.getSystemResource().getProperties().getName());
                newSystem = system;
            }
        }

        // Update the system
        SystemResource updatedProperties = new SystemResource(systemId, new Properties(null, "urn:sensor:cat_sensor_001", "Cat Sensor", null, "asdfg", List.of("2024-12-06T18:57:51.968Z", "now"), null));
        newSystem.updateSystem(updatedProperties);

        // Delete the system
        assertTrue(node.deleteSystem(newSystem));
        System.out.println("System deleted " + systemId + ": " + newSystem.getSystemResource().getProperties().getName());
        System.out.println();
    }

    @Test
    void testDatastreamListener() throws InterruptedException {
        System.out.println();
        System.out.println("Datastream listener test");

        oshConnect.discoverSystems();
        List<OSHDatastream> datastreams = oshConnect.discoverDatastreams();
        assertFalse(datastreams.isEmpty(), "No datastreams found.");

        DatastreamHandler handler = oshConnect.getDatastreamManager().createDatastreamHandler(this::onStreamUpdate);

        // Add all the discovered datastreams to the handler.
        for (OSHDatastream datastream : datastreams) {
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
