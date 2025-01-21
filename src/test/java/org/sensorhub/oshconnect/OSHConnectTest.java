package org.sensorhub.oshconnect;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sensorhub.oshconnect.TestConstants.*;

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
        DataStreamManager dataStreamManager = oshConnect.getDataStreamManager();
        dataStreamManager.createDataStreamHandler(args -> {
            // Do nothing
        });
        oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        oshConnect.createNode("different.url", IS_SECURE, USERNAME, PASSWORD);

        assertEquals(2, oshConnect.getNodeManager().getNodes().size());
        assertEquals(1, dataStreamManager.getDataStreamHandlers().size());
        oshConnect.shutdown();
        assertEquals(0, oshConnect.getNodeManager().getNodes().size());
        assertEquals(0, dataStreamManager.getDataStreamHandlers().size());
    }
}