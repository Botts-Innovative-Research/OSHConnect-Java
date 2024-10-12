package org.sensorhub.oshconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.net.websocket.DatastreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DatastreamHandler;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.util.List;
import java.util.UUID;

class OSHConnectTest {
    private OSHConnect oshConnect;
    private OSHNode node1;
    private OSHNode node2;
    private UUID node1Id;

    @BeforeEach
    void setUp() {
        oshConnect = new OSHConnect();
        node1Id = UUID.randomUUID();
        node1 = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD, node1Id);
        node2 = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE);
    }

    @AfterEach
    void tearDown() {
        oshConnect.shutdown();
    }

    @Test
    void addNode() {
        assertEquals(0, oshConnect.getNodes().size());
        oshConnect.addNode(node1);
        assertEquals(1, oshConnect.getNodes().size());
        oshConnect.addNode(node2);
        assertEquals(2, oshConnect.getNodes().size());
    }

    @Test
    void addNode_Duplicate() {
        assertEquals(0, oshConnect.getNodes().size());
        oshConnect.addNode(node1);
        assertEquals(1, oshConnect.getNodes().size());
        oshConnect.addNode(node1);
        assertEquals(1, oshConnect.getNodes().size());
    }

    @Test
    void addNodes() {
        assertEquals(0, oshConnect.getNodes().size());
        oshConnect.addNodes(List.of(node1, node2));
        assertEquals(2, oshConnect.getNodes().size());
    }

    @Test
    void removeNode() {
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);
        assertEquals(2, oshConnect.getNodes().size());
        oshConnect.removeNode(node1);
        assertEquals(1, oshConnect.getNodes().size());
    }

    @Test
    void removeNode_ByUUID() {
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);
        assertEquals(2, oshConnect.getNodes().size());
        oshConnect.removeNode(node1Id);
        assertEquals(1, oshConnect.getNodes().size());
    }

    @Test
    void removeNode_ByUUID_NotFound() {
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);
        assertEquals(2, oshConnect.getNodes().size());
        oshConnect.removeNode(UUID.randomUUID());
        assertEquals(2, oshConnect.getNodes().size());
    }

    @Test
    void removeAllNodes() {
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);
        assertEquals(2, oshConnect.getNodes().size());
        oshConnect.removeAllNodes();
        assertEquals(0, oshConnect.getNodes().size());
    }

    @Test
    void getNode() {
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);
        assertEquals(node1, oshConnect.getNode(node1Id));
    }

    @Test
    void getNodes() {
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);
        assertEquals(2, oshConnect.getNodes().size());
    }

    @Test
    void getNode_NotFound() {
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);
        assertNull(oshConnect.getNode(UUID.randomUUID()));
    }

    @Test
    void createDatastreamHandler() {
        oshConnect.createDatastreamHandler(args -> {
            // Do nothing
        });
        assertEquals(1, oshConnect.getDatastreamHandlers().size());
    }

    @Test
    void addDatastreamHandler() {
        DatastreamHandler handler = new DatastreamHandler() {
            @Override
            public void onStreamUpdate(DatastreamEventArgs args) {
                // Do nothing
            }
        };
        assertEquals(0, oshConnect.getDatastreamHandlers().size());
        oshConnect.addDatastreamHandler(handler);
        assertEquals(1, oshConnect.getDatastreamHandlers().size());
    }

    @Test
    void shutdownDatastreamHandler() {
        DatastreamHandler handler = oshConnect.createDatastreamHandler(args -> {
            // Do nothing
        });

        assertEquals(1, oshConnect.getDatastreamHandlers().size());
        oshConnect.shutdownDatastreamHandler(handler);
        assertEquals(0, oshConnect.getDatastreamHandlers().size());

        assertThrows(IllegalStateException.class, handler::connect);
    }

    @Test
    void shutdownDatastreamHandlers() {
        oshConnect.createDatastreamHandler(args -> {
            // Do nothing
        });
        oshConnect.createDatastreamHandler(args -> {
            // Do nothing
        });

        assertEquals(2, oshConnect.getDatastreamHandlers().size());
        oshConnect.shutdownDatastreamHandlers();
        assertEquals(0, oshConnect.getDatastreamHandlers().size());
    }

    @Test
    void shutdown() {
        oshConnect.createDatastreamHandler(args -> {
            // Do nothing
        });
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);

        assertEquals(2, oshConnect.getNodes().size());
        assertEquals(1, oshConnect.getDatastreamHandlers().size());
        oshConnect.shutdown();
        assertEquals(0, oshConnect.getNodes().size());
        assertEquals(0, oshConnect.getDatastreamHandlers().size());
    }
}