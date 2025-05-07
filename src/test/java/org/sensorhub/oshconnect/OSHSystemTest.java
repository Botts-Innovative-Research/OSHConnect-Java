package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.notification.INotificationDataStream;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.sensorhub.oshconnect.tools.ControlStreamTools.newCommandStreamInfo;
import static org.sensorhub.oshconnect.tools.DataStreamTools.newDataStreamInfo;
import static org.sensorhub.oshconnect.tools.SystemTools.newSystem;

class OSHSystemTest extends TestBase {
    private OSHSystem system;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException {
        system = node.createSystem(newSystem());
        assertNotNull(system);
    }

    @Test
    void createSystem() {
        assertNotNull(system);
        assertNotNull(system.getSystemResource());
    }

    @Test
    void updateSystem() throws ExecutionException, InterruptedException {
        var result = system.updateSystem(newSystem("Updated Name", "Updated Description"));
        assertTrue(result);
        assertAll(
                () -> assertEquals("Updated Name", system.getSystemResource().getName()),
                () -> assertEquals("Updated Description", system.getSystemResource().getDescription())
        );
    }

    @Test
    void refreshSystem() throws ExecutionException, InterruptedException {
        // Update the system outside the context of OSHConnect
        var updatedSystem = newSystem("Updated Name", "Updated Description");
        Integer response = node.getConnectedSystemsApiClient().updateSystem(system.getId(), updatedSystem).get();
        assertNotNull(response);
        assertEquals(204, response);

        var result = system.refreshSystem();
        assertTrue(result);
        assertAll(
                () -> assertEquals("Updated Name", system.getSystemResource().getName()),
                () -> assertEquals("Updated Description", system.getSystemResource().getDescription())
        );
    }

    @Test
    void deleteSystem() {
        var result = node.deleteSystem(system);
        assertTrue(result);
        var systems = node.getSystems();
        assertNotNull(systems);
        assertFalse(systems.stream().anyMatch(s -> s.getSystemResource().getId().equals(system.getSystemResource().getId())));
    }

    @Test
    void createDataStream() throws ExecutionException, InterruptedException {
        var dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);
        assertNotNull(dataStream.getDataStreamResource());
    }

    @Test
    void deleteDataStream() throws ExecutionException, InterruptedException {
        var dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);
        var result = system.deleteDataStream(dataStream);
        assertTrue(result);
        var dataStreams = system.getDataStreams();
        assertTrue(dataStreams.isEmpty());
    }

    @Test
    void discoverDataStreams() throws ExecutionException, InterruptedException {
        var dataStreams = system.discoverDataStreams();
        assertTrue(dataStreams.isEmpty());

        // Create a data stream outside the context of OSHConnect
        String id = node.getConnectedSystemsApiClient().addDataStream(system.getId(), newDataStreamInfo()).get();
        assertNotNull(id);

        dataStreams = system.discoverDataStreams();
        assertEquals(1, dataStreams.size());
        assertEquals(id, dataStreams.get(0).getId());
    }

    @Test
    void discoverControlStreams() throws ExecutionException, InterruptedException {
        var controlStreams = system.discoverControlStreams();
        assertTrue(controlStreams.isEmpty());

        // Create a control stream outside the context of OSHConnect
        String id = node.getConnectedSystemsApiClient().addControlStream(system.getId(), newCommandStreamInfo()).get();
        assertNotNull(id);

        controlStreams = system.discoverControlStreams();
        assertEquals(1, controlStreams.size());
        assertEquals(id, controlStreams.get(0).getId());
    }

    @Test
    void notification_AddDataStream() throws ExecutionException, InterruptedException {
        var notificationManager = oshConnect.getNotificationManager();
        var added = new boolean[1];
        var listener = createDataStreamNotificationListener(added, new boolean[1]);
        notificationManager.addDataStreamNotificationListener(listener);

        assertFalse(added[0]);
        system.createDataStream(newDataStreamInfo());
        assertTrue(added[0]);
    }

    @Test
    void notification_DeleteDataStream() throws ExecutionException, InterruptedException {
        var notificationManager = oshConnect.getNotificationManager();
        var removed = new boolean[1];
        var listener = createDataStreamNotificationListener(new boolean[1], removed);
        notificationManager.addDataStreamNotificationListener(listener);

        var dataStream = system.createDataStream(newDataStreamInfo());
        assertFalse(removed[0]);
        system.deleteDataStream(dataStream);
        assertTrue(removed[0]);
    }

    INotificationDataStream createDataStreamNotificationListener(boolean[] added, boolean[] removed) {
        return new INotificationDataStream() {
            @Override
            public void onItemAdded(OSHDataStream item) {
                added[0] = true;
            }

            @Override
            public void onItemRemoved(OSHDataStream item) {
                removed[0] = true;
            }
        };
    }
}
