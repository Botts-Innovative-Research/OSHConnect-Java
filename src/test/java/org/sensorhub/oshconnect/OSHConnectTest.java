package org.sensorhub.oshconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OSHConnectTest {
    private OSHConnect oshConnect;

    @BeforeEach
    void setUp() {
        oshConnect = new OSHConnect();
    }

    @AfterEach
    void tearDown() {
        oshConnect.shutdown();
    }

    @Test
    void createNode() {
        assertEquals(0, oshConnect.getNodeManager().getNodes().size());
        oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        assertEquals(1, oshConnect.getNodeManager().getNodes().size());
    }

    @Test
    void shutdown() {
        DatastreamManager datastreamManager = oshConnect.getDatastreamManager();
        datastreamManager.createDatastreamHandler(args -> {
            // Do nothing
        });
        oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        oshConnect.createNode("different.url", IS_SECURE, USERNAME, PASSWORD);

        assertEquals(2, oshConnect.getNodeManager().getNodes().size());
        assertEquals(1, datastreamManager.getDatastreamHandlers().size());
        oshConnect.shutdown();
        assertEquals(0, oshConnect.getNodeManager().getNodes().size());
        assertEquals(0, datastreamManager.getDatastreamHandlers().size());
    }
}