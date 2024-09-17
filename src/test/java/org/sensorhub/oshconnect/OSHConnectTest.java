package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.Node;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OSHConnectTest {
    private OSHConnect oshConnect;
    private Node node1;
    private Node node2;
    private UUID node1Id;

    @BeforeEach
    void setUp() {
        oshConnect = new OSHConnect();
        node1Id = UUID.randomUUID();
        node1 = new Node("http://localhost:8181/sensorhub", "admin", "admin", node1Id);
        node2 = new Node("http://localhost:8181/sensorhub");
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
}