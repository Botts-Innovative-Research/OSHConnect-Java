package org.sensorhub.oshconnect;

import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.Datastream;
import org.sensorhub.oshconnect.datamodels.Node;
import org.sensorhub.oshconnect.net.websocket.WebSocketWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.sensorhub.oshconnect.TestConstants.*;

public class WebSocketTest {
    @Test
    void testConnect() throws InterruptedException {
        OSHConnect oshConnect = new OSHConnect();
        Node node = new Node(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        oshConnect.addNode(node);

        List<Datastream> datastreams = node.discoverDatastreams();
        CountDownLatch latch = new CountDownLatch(datastreams.size());
        List<WebSocketWorker> workers = new ArrayList<>();

        for (Datastream datastream : datastreams) {
            System.out.println("Datastream: " + datastream);

            String request = "/datastreams/" + datastream.getId() + "/observations?format=application/json";

            System.out.println("Request: " + request);

            WebSocketWorker worker = new WebSocketWorker(datastream, request);
            worker.connect();
            workers.add(worker);
        }

        latch.await(5, TimeUnit.SECONDS);

        for (WebSocketWorker worker : workers) {
            worker.disconnect();
        }
    }
}
