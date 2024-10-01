package org.sensorhub.oshconnect;

import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.net.websocket.WebSocketWorker;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.sensorhub.oshconnect.TestConstants.*;

class WebSocketTest {
    @Test
    void testConnect() throws InterruptedException {
        OSHConnect oshConnect = new OSHConnect();
        OSHNode oshNode = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        oshConnect.addNode(oshNode);
        oshConnect.discoverSystems();
        oshConnect.discoverDatastreams();

        List<OSHDatastream> datastreams = oshConnect.getDatastreams();
        CountDownLatch latch = new CountDownLatch(datastreams.size());
        List<WebSocketWorker> workers = new ArrayList<>();

        for (OSHDatastream datastream : datastreams) {
            System.out.println("Datastream: " + datastream.getDatastreamResource());

            String request = "/datastreams/" + datastream.getId() + "/observations?format=application/json";

            System.out.println("Request: " + request);

            WebSocketWorker worker = new WebSocketWorker(datastream, request);
            worker.connect();
            workers.add(worker);
        }

        latch.await(1, TimeUnit.SECONDS);

        for (WebSocketWorker worker : workers) {
            worker.disconnect();
        }
    }
}
