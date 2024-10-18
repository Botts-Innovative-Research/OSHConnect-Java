package org.sensorhub.oshconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        node1.setName("Node 1");
        node2 = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE);
        node2.setName("Node 2");
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

        var duplicateNode = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD, node1Id);
        oshConnect.addNode(duplicateNode);
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
    void shutdown() {
        DatastreamManager datastreamManager = oshConnect.getDatastreamManager();
        datastreamManager.createDatastreamHandler(args -> {
            // Do nothing
        });
        oshConnect.addNode(node1);
        oshConnect.addNode(node2);

        assertEquals(2, oshConnect.getNodes().size());
        assertEquals(1, datastreamManager.getDatastreamHandlers().size());
        oshConnect.shutdown();
        assertEquals(0, oshConnect.getNodes().size());
        assertEquals(0, datastreamManager.getDatastreamHandlers().size());
    }
}