package org.sensorhub.oshconnect.datamodels;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.sensorhub.oshconnect.TestConstants.*;

class NodeTest {
    @Test
    void discoverSystems() {
        Node node = new Node(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        List<System> systems = node.discoverSystems();
        for (System system : systems) {
            java.lang.System.out.println("System: " + system);
        }
    }

    @Test
    void discoverDatastreams() {
        Node node = new Node(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        List<Datastream> datastreams = node.discoverDatastreams();
        for (Datastream datastream : datastreams) {
            java.lang.System.out.println("Datastream: " + datastream);
        }
    }
}