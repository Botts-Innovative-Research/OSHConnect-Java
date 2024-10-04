package org.sensorhub.oshconnect;

import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.Observation;
import org.sensorhub.oshconnect.net.websocket.DatastreamListener;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
                    Observation observation = getObservationFromJson(data);
                    System.out.println("Datastream update: " + observation.getResult());
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
