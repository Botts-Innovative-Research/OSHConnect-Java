package org.sensorhub.oshconnect;

import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.Observation;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.net.websocket.DatastreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DatastreamHandler;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.time.TimeExtent;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class WebSocketTest {
    // This test requires a live OpenSensorHub instance to connect to.
    // Check the constants in TestConstants.java to ensure they are correct
    // and uncomment the @Disabled annotation to run the test.
    @Disabled
    @Test
    void testConnect() throws InterruptedException {
        OSHConnect oshConnect = new OSHConnect();
        OSHNode oshNode = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);

        oshConnect.addNode(oshNode);
        oshConnect.discoverSystems();
        List<OSHDatastream> datastreams = oshConnect.discoverDatastreams();
        DatastreamHandler handler = oshConnect.getDatastreamManager().createDatastreamHandler(this::onStreamUpdate);

        // Add all the discovered datastreams to the handler.
        datastreams.forEach(handler::addDatastream);

        // Connect, listen for updates.
        handler.connect();
        CountDownLatch latch = new CountDownLatch(datastreams.size());
        latch.await(3, TimeUnit.SECONDS);

        // Start listening for historical data instead of live data.
        Instant oneMinuteAgo = Instant.now().minusSeconds(60);
        handler.setTimeExtent(TimeExtent.startingAt(oneMinuteAgo));
        handler.setReplaySpeed(0.25);
        latch.await(3, TimeUnit.SECONDS);

        oshConnect.shutdown();
    }

    private void onStreamUpdate(DatastreamEventArgs args) {
        var datastreamId = args.getDatastream().getDatastreamResource().getId();
        var timestamp = args.getTimestamp();
        if (args.getFormat() == RequestFormat.JSON) {
            Observation observation = Observation.fromJson(args.getData());
            System.out.println("onStreamUpdate: timestamp=" + timestamp + " datastreamId=" + datastreamId + " observation=" + observation);
        } else {
            System.out.println("onStreamUpdate: timestamp=" + timestamp + " datastreamId=" + datastreamId + " data=binary");
        }
    }
}
