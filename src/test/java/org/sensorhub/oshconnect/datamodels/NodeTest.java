package org.sensorhub.oshconnect.datamodels;

import org.junit.jupiter.api.Test;

import java.util.List;

class NodeTest {
    String sensorHubRoot = "http://localhost:8181/sensorhub";
    String username = "admin";
    String password = "admin";

//    String sensorHubRoot = "https://api.georobotix.io/ogc/t18";
//    String username = null;
//    String password = null;

    @Test
    void discoverSystems() {
        Node node = new Node(sensorHubRoot, "admin", "admin");
//        Node node = new Node("https://api.georobotix.io/ogc/t18");
        List<System> systems = node.discoverSystems();
        for (System system : systems) {
            java.lang.System.out.println("System: " + system);
        }
    }

    @Test
    void discoverDatastreams() {
        Node node = new Node(sensorHubRoot, "admin", "admin");
        List<Datastream> datastreams = node.discoverDatastreams();
        for (Datastream datastream : datastreams) {
            java.lang.System.out.println("Datastream: " + datastream);
        }
    }
}