package org.sensorhub.oshconnect;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.sensorhub.oshconnect.TestConstants.*;

class NodeManagerTest {
    private OSHConnect oshConnect;
    private NodeManager nodeManager;
    private OSHNode node1;
    private OSHNode node2;
    private UUID node1Id;

    @BeforeEach
    void setUp() {
        oshConnect = new OSHConnect();
        nodeManager = oshConnect.getNodeManager();
        node1Id = UUID.randomUUID();
        node1 = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD, node1Id);
        node2 = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
    }

    @AfterEach
    void tearDown() {
        oshConnect.shutdown();
    }

    @Test
    void addNode() {
        assertEquals(0, nodeManager.getNodes().size());
        nodeManager.addNode(node1);
        assertEquals(1, nodeManager.getNodes().size());
        nodeManager.addNode(node2);
        assertEquals(2, nodeManager.getNodes().size());
    }

    @Test
    void addNode_Duplicate() {
        assertEquals(0, nodeManager.getNodes().size());
        nodeManager.addNode(node1);
        assertEquals(1, nodeManager.getNodes().size());
        nodeManager.addNode(node1);
        assertEquals(1, nodeManager.getNodes().size());

        var duplicateNode = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD, node1Id);
        nodeManager.addNode(duplicateNode);
        assertEquals(1, nodeManager.getNodes().size());
    }

    @Test
    void addNodes() {
        assertEquals(0, nodeManager.getNodes().size());
        nodeManager.addNodes(List.of(node1, node2));
        assertEquals(2, nodeManager.getNodes().size());
    }

    @Test
    void removeNode() {
        nodeManager.addNode(node1);
        nodeManager.addNode(node2);
        assertEquals(2, nodeManager.getNodes().size());
        nodeManager.removeNode(node1);
        assertEquals(1, nodeManager.getNodes().size());
    }

    @Test
    void removeNode_ByUUID() {
        nodeManager.addNode(node1);
        nodeManager.addNode(node2);
        assertEquals(2, nodeManager.getNodes().size());
        nodeManager.removeNode(node1Id);
        assertEquals(1, nodeManager.getNodes().size());
    }

    @Test
    void removeNode_ByUUID_NotFound() {
        nodeManager.addNode(node1);
        nodeManager.addNode(node2);
        assertEquals(2, nodeManager.getNodes().size());
        nodeManager.removeNode(UUID.randomUUID());
        assertEquals(2, nodeManager.getNodes().size());
    }

    @Test
    void removeAllNodes() {
        nodeManager.addNode(node1);
        nodeManager.addNode(node2);
        assertEquals(2, nodeManager.getNodes().size());
        nodeManager.removeAllNodes();
        assertEquals(0, nodeManager.getNodes().size());
    }

    @Test
    void getNode() {
        nodeManager.addNode(node1);
        nodeManager.addNode(node2);
        assertEquals(node1, nodeManager.getNode(node1Id));
    }

    @Test
    void getNodes() {
        nodeManager.addNode(node1);
        nodeManager.addNode(node2);
        assertEquals(2, nodeManager.getNodes().size());
    }

    @Test
    void getNode_NotFound() {
        nodeManager.addNode(node1);
        nodeManager.addNode(node2);
        assertNull(nodeManager.getNode(UUID.randomUUID()));
    }
}
