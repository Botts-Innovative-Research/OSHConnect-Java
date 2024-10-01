package org.sensorhub.oshconnect.oshdatamodels;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.sensorhub.oshconnect.TestConstants.*;

class OSHNodeTest {
    @Test
    void discoverSystems() {
        OSHNode oshNode = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        oshNode.discoverSystems();
        List<OSHSystem> systems = oshNode.getSystems();
        for (OSHSystem system : systems) {
            java.lang.System.out.println("System: " + system.getSystemResource());
        }
    }

    @Test
    void discoverDatastreams() {
        OSHNode oshNode = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        oshNode.discoverSystems();
        oshNode.discoverDatastreams();
        List<OSHDatastream> datastreams = oshNode.getDatastreams();
        for (OSHDatastream datastream : datastreams) {
            java.lang.System.out.println("Datastream: " + datastream.getDatastreamResource());
        }
    }
}