package org.sensorhub.oshconnect;

import net.opengis.swe.v20.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.sensorhub.impl.SensorHub;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.net.websocket.StreamHandler;
import org.sensorhub.oshconnect.util.SystemsQueryBuilder;

import java.lang.Thread;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class LiveTest {

    private OSHNode node = null;

    final private AtomicInteger testNumFrames = new AtomicInteger(5);

    final private AtomicInteger testNumGpsCoordinates = new AtomicInteger(2);

    final private AtomicBoolean failed = new AtomicBoolean(false);

    private OSHConnect oshConnect = null;

    private enum SystemType {
        FRAME, GPS
    }

    private Thread thread = null;

    @BeforeEach
    void createNode() {

        try {
            thread = new Thread(() -> {
                System.out.println("Starting OSH");

                SensorHub.main(new String[] {"src/main/resources/testconfig.json",
                        "storage"});

                while ( true ) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // restore flag
                        return;
                    }
                }
            });

            thread.start();

            Thread.sleep(2000);
            oshConnect = new OSHConnect();
            boolean isSecure = false;
            String lUrl = "http://127.0.0.1:8181/sensorhub";
            node = oshConnect.createNode(lUrl, isSecure, "anonymous", "anonymous");

            //mstinson:testing - this is only for adding configurations
//            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void removeNode() {
        oshConnect.shutdown();
    }


    void validateGpsData( byte[] data  ) {

        var dataObject = new JSONObject(new String(data));
        assertFalse(dataObject.isEmpty());

        var result = dataObject.optJSONObject("result");
        assertFalse(result.isEmpty());

        var location = result.optJSONObject("location");
        assertFalse(location.isEmpty());

        var alt = location.getFloat("alt");
        var altDelta = Math.abs(193.0 - alt);
        assertFalse( altDelta > 0.0001 );

        var lat = location.getFloat("lat");
        var latDelta = Math.abs(34.73 - lat);
        assertFalse( latDelta > 0.1);

        var lon = location.getFloat("lon");
        var lonDelta = Math.abs(-86.60 - lon);
        assertFalse( lonDelta > 0.1);

        testNumGpsCoordinates.set(testNumGpsCoordinates.get() - 1);

        System.out.println("Gps Data: Lat: " + lat + " Lon: " + lon + "\n");
    }

    void validateFrame( byte[] data ) {

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
    void TestVideoFrame() {
        String systemId = "036o58vcplkg"; //ffmpeg driver system id

        TestSystem(systemId, SystemType.FRAME, testNumFrames);
    }

    @Test
    void TestGps() {
        String systemId = "03uq775pjdog"; //ffmpeg driver system id

        TestSystem(systemId, SystemType.GPS, testNumGpsCoordinates);
    }

    private void TestSystem( String systemId, SystemType systemType, AtomicInteger sampleCount ) {
        assert null != oshConnect;

        CompletableFuture
                .supplyAsync(() -> {
                    try {

                        var ids = new ArrayList<String>();
                        ids.add(systemId);
                        var query = new SystemsQueryBuilder().id(ids);

                        return node != null ? node.discoverSystems(query) : null;
                    } catch ( ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(systemsResult -> {
                    if (systemsResult == null || systemsResult.isEmpty()) return;

                    var dataStreamManager = oshConnect.getDataStreamManager();
                    StreamHandler streamHandler = dataStreamManager.createDataStreamHandler(( args ) -> {

                        switch (systemType) {
                            case FRAME -> validateFrame(args.getData());
                            case GPS -> validateGpsData(args.getData());
                        }
                    });

                    switch (systemType) {
                        case FRAME -> streamHandler.setRequestFormat(RequestFormat.SWE_BINARY);
                        case GPS -> streamHandler.setRequestFormat(RequestFormat.OM_JSON);
                    }

                    OSHSystem cSystem = systemsResult.get(0);
                    assert cSystem.getId().equals(systemId);

                    System.out.println("OSHSystemID OpenSensorHub System Found. ID: " + cSystem.getId());

                    try {
                        var dataStreams = cSystem.discoverDataStreams();

                        var aDs = dataStreams.get(0);

                        assert aDs != null;
                        streamHandler.addDataStreamListener(aDs);

                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    streamHandler.connect();

                    while (sampleCount.get() > 0 && !failed.get()) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ignored) {}
                    }

                    streamHandler.shutdown();

                    if ( thread.isAlive()) {
                        thread.interrupt();
                    }

                    if ( failed.get() ) {
                        System.out.println("Test FAILED\n");
                    } else {
                        System.out.println("Test PASSED\n");
                    }

                    assertEquals(failed.get(), false);
                })
                .exceptionally(ex -> {
                    System.out.println("OSHSystemID Exception thrown: " + ex.getMessage());
                    assert false;
                    return null;
                })
                .join();
    }




}
