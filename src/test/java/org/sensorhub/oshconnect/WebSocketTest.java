package org.sensorhub.oshconnect;

import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.net.websocket.DatastreamListener;
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
        List<DatastreamListener> datastreamListeners = new ArrayList<>();

        for (OSHDatastream datastream : datastreams) {
            System.out.println("Datastream: " + datastream.getDatastreamResource());

            String request = datastream.getObservationsEndpoint();

            System.out.println("Request: " + request);

            DatastreamListener listener = new DatastreamListener(datastream, request) {
                @Override
                public void onStreamUpdate(byte[] data) {
                    System.out.println("Datastream update: " + new String(data));
                }
            };

            listener.connect();
            datastreamListeners.add(listener);
        }

        latch.await(3, TimeUnit.SECONDS);

        for (DatastreamListener datastreamListener : datastreamListeners) {
            datastreamListener.disconnect();
        }
    }
}
