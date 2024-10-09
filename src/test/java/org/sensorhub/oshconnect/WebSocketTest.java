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

import java.time.Instant;
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
        List<OSHDatastream> datastreams = oshConnect.discoverDatastreams();

        CountDownLatch latch = new CountDownLatch(datastreams.size());
        List<DatastreamListener> datastreamListeners = new ArrayList<>();

        for (OSHDatastream datastream : datastreams) {
            System.out.println("Datastream: " + datastream.getDatastreamResource());

            DatastreamListener listener = new DatastreamListener(datastream) {
                @Override
                public void onStreamJson(long timestamp, Observation observation) {
                    System.out.println("  onStreamJson: timestamp=" + Instant.ofEpochMilli(timestamp) + ", observation=" + observation);
                }

                @Override
                public void onStreamBinary(long timestamp, byte[] data) {
                    System.out.println("onStreamBinary: timestamp=" + Instant.ofEpochMilli(timestamp) + ", data=binary");
                }

                @Override
                public void onStreamCsv(long timestamp, String csv) {
                    System.out.println("   onStreamCsv: timestamp=" + Instant.ofEpochMilli(timestamp) + ", csv=" + csv);
                }

                @Override
                public void onStreamXml(long timestamp, String xml) {
                    System.out.println("   onStreamXml: timestamp=" + Instant.ofEpochMilli(timestamp) + ", xml=" + xml);
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
