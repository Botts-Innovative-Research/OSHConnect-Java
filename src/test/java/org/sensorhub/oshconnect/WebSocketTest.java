package org.sensorhub.oshconnect;

import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.Observation;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.net.websocket.DatastreamHandler;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.time.TimeExtent;

import java.time.Instant;
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

        DatastreamHandler handler = oshConnect.createDatastreamHandler(args -> {
            var datastreamId = args.getDatastream().getDatastreamResource().getId();
            var timestamp = args.getTimestamp();
            if (args.getFormat() == RequestFormat.JSON) {
                Observation observation = Observation.fromJson(args.getData());
                System.out.println("onStreamUpdate: timestamp=" + timestamp + " datastreamId=" + datastreamId + " observation=" + observation);
            } else {
                System.out.println("onStreamUpdate: timestamp=" + timestamp + " datastreamId=" + datastreamId + " data=binary");
            }
        });

        // Add all the discovered datastreams to the handler.
        for (OSHDatastream datastream : datastreams) {
            handler.addDatastream(datastream);
        }

        // Connect, listen for updates.
        handler.connect();
        latch.await(3, TimeUnit.SECONDS);

        // Start listening for historical data instead of live data.
        Instant oneMinuteAgo = Instant.now().minusSeconds(60);
        handler.setTimeExtent(TimeExtent.startingAt(oneMinuteAgo));
        handler.setReplaySpeed(0.2);
        latch.await(3, TimeUnit.SECONDS);

        oshConnect.shutdownDatastreamHandlers();
    }
}
