package org.sensorhub.oshconnect;

import net.opengis.swe.v20.*;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.sensorhub.impl.SensorHub;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.net.websocket.StreamHandler;

import java.lang.Thread;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class LiveTest {

    private OSHNode node = null;

    final private AtomicInteger testNumFrames = new AtomicInteger(20);
    final private AtomicBoolean failed = new AtomicBoolean(false);

    @BeforeEach
    void createNode() {

//        var oshConnect = new OSHConnect();
//
//        boolean isSecure = true;
//        String lUrl = "https://osh-dev.botts-inc.com:8443/sensorhub";
//        node = oshConnect.createNode(lUrl, isSecure, "anonymous", "anonymous");
    }

    void update( byte[] data ) {

        byte[] resultBytes;

        try {
            // SWE Common Header is Timestamp/BufferSize/Buffer
            // [8 bytes for timestamp in double format,
            // 4 bytes for frame size,
            // then frame starts at index 12]
            var buffer = ByteBuffer.wrap(data);
            double timeStamp = buffer.getDouble(0);
            //System.out.printf("%016X%n", timeStamp); //1761863827983
            long timeStampMillis = (long)(timeStamp * 1000);
            //Instant instant = Instant.ofEpochSecond((long)timeStamp);
            Instant instant = Instant.ofEpochMilli(timeStampMillis);
            int frameSize = buffer.getInt(8);

            //check to see if date is reasonable
            Instant start = LocalDate.of(2012, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end   = LocalDate.of(2040, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);

            boolean inRange = !instant.isBefore(start) && instant.isBefore(end);

            if ( !inRange ) {
                failed.set(true);
            }

            //alternatively check max size as well
            if ( !(frameSize > 0) ) {
                failed.set(true);
            }

            //packets start with 2 bytes of 0
            if ( 0x00 != buffer.get(12) ||
                    0x00 != buffer.get(13) ) {
                failed.set(true);
            }

            testNumFrames.set(testNumFrames.get()-1);

            System.out.println("CLIENT Timestamp: " + instant.toString() + " CLIENT Framesize: " + frameSize + "\n");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //System.out.println("Parsed data: \n" + dataObject);
    }

    @Test
    void TestAgainstLiveNode() {


        try {
            Thread thread = new Thread(() -> {
                System.out.println("Starting OSH");

                SensorHub.main(new String[] {"src/main/resources/testconfig.json",
                        "storage"});

                while ( true ) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            thread.start();

            Thread.sleep(2000);
            var oshConnect = new OSHConnect();
            boolean isSecure = false;
            String lUrl = "http://127.0.0.1:8181/sensorhub";
            node = oshConnect.createNode(lUrl, isSecure, "anonymous", "anonymous");



            CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            return node != null ? node.discoverSystems() : null;
                        } catch ( ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .thenAccept(systemsResult -> {
                        if (systemsResult == null) return;

                        var dataStreamManager = oshConnect.getDataStreamManager();
                        StreamHandler streamHandler = dataStreamManager.createDataStreamHandler(( args ) -> {
                            update(args.getData());
                        });

                        streamHandler.setRequestFormat(RequestFormat.SWE_BINARY);

                        for (OSHSystem cSystem : systemsResult) {
                            System.out.println("OSHSystemID OpenSensorHub System Found. ID: " + cSystem.getId());

                            try {
                                var dataStreams = cSystem.discoverDataStreams();

                                var a = dataStreams.get(0);
                                for ( var ds : dataStreams ) {

                                    //start mstinson:testing
                                    //var observations = ds.getObservations();
                                    //end mstinson:testing

                                    streamHandler.addDataStreamListener(ds);
                                }

                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        streamHandler.connect();

                        while (testNumFrames.get() >= 0 && !failed.get()) {
                            try {
                                Thread.sleep(50); // small polling interval
                            } catch (InterruptedException ignored) {}
                        }

                        streamHandler.shutdown();

                        if ( failed.get() ) {
                            System.out.println("Test FAILED\n");
                        } else {
                            System.out.println("Test PASSED\n");
                        }

                        assertEquals(failed.get(), false);
                    })
                    .exceptionally(ex -> {
                        System.out.println("OSHSystemID Exception thrown: " + ex.getMessage());
                        assertEquals(false, true);
                        return null;
                    })
                    .join();

            oshConnect.shutdown();

            //thread.join();

        } catch (InterruptedException e ) {
            throw new RuntimeException(e);
        }
    }
}
