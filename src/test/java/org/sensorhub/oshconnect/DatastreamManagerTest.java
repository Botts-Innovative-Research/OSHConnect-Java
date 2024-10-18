package org.sensorhub.oshconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.net.websocket.DatastreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DatastreamHandler;

class DatastreamManagerTest {
    private OSHConnect oshConnect;
    private DatastreamManager datastreamManager;

    @BeforeEach
    void setUp() {
        oshConnect = new OSHConnect();
        datastreamManager = oshConnect.getDatastreamManager();
    }

    @AfterEach
    void tearDown() {
        oshConnect.shutdown();
    }

    @Test
    void createDatastreamHandler() {
        datastreamManager.createDatastreamHandler(args -> {
            // Do nothing
        });
        assertEquals(1, datastreamManager.getDatastreamHandlers().size());
    }

    @Test
    void addDatastreamHandler() {
        DatastreamHandler handler = new DatastreamHandler() {
            @Override
            public void onStreamUpdate(DatastreamEventArgs args) {
                // Do nothing
            }
        };
        assertEquals(0, datastreamManager.getDatastreamHandlers().size());
        datastreamManager.addDatastreamHandler(handler);
        assertEquals(1, datastreamManager.getDatastreamHandlers().size());
    }

    @Test
    void shutdownDatastreamHandler() {
        DatastreamHandler handler = datastreamManager.createDatastreamHandler(args -> {
            // Do nothing
        });

        assertEquals(1, datastreamManager.getDatastreamHandlers().size());
        datastreamManager.shutdownDatastreamHandler(handler);
        assertEquals(0, datastreamManager.getDatastreamHandlers().size());

        assertThrows(IllegalStateException.class, handler::connect);
    }

    @Test
    void shutdownDatastreamHandlers() {
        datastreamManager.createDatastreamHandler(args -> {
            // Do nothing
        });
        datastreamManager.createDatastreamHandler(args -> {
            // Do nothing
        });

        assertEquals(2, datastreamManager.getDatastreamHandlers().size());
        datastreamManager.shutdownDatastreamHandlers();
        assertEquals(0, datastreamManager.getDatastreamHandlers().size());
    }
}
