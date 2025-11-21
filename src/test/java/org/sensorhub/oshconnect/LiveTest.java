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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class starts a live OSH node with some precaptured simulated data, queries it
 * through the OSHConnect-Java library, and validates some of the samples. Recommend
 * we keep adding data types to this as time allows.
 *
 * The current validated data is:
 * 5 frames of h.264 video stream pulled from an *.mp4 video
 * 2 samples of generic custom data from a template driver
 * 2 samples of simulated weather data
 *
 * The test should run without any modifications. If this test fails then something inside
 * OSHConnect-Java has broken.
 */
public class LiveTest {

    private OSHNode node = null;

    final private AtomicInteger testNumFrames = new AtomicInteger(5);
    final private AtomicInteger testNumGenericDataPoints = new AtomicInteger(2);
    final private AtomicInteger testNumWeatherCoordinates = new AtomicInteger(2);

    final private AtomicBoolean failed = new AtomicBoolean(false);

    private OSHConnect oshConnect = null;

    private enum SystemType {
        FRAME, GENERIC, WEATHER
    }

    private Thread thread = null;

    @BeforeEach
    void createNode() {

        try {
            thread = new Thread(() -> {
                System.out.println("Starting OSH");

                String rootDir = System.getProperty("user.dir");
                System.setProperty("osh.root", rootDir);

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

            //mstinson:testing - this is for adding configurations
//            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void removeNode() {
        oshConnect.shutdown();
    }

    void validateWeatherData( byte[] data ) {
        var dataObject = new JSONObject(new String(data));
        assertFalse(dataObject.isEmpty());

        var result = dataObject.optJSONObject("result");
        assertFalse(result.isEmpty());

        var temperature = result.getFloat("temperature");
        assertFalse( temperature < 0.0 );

        var pressure = result.getFloat("pressure");
        assertFalse( pressure < 0.0 );

        var windDirection = result.getFloat("windDirection");
        assertFalse( windDirection < 0.0 );

        var windSpeed = result.getFloat("windSpeed");
        assertFalse( windSpeed < 0.0 );

    }

    void validateGenericData( byte[] data  ) {

        var dataObject = new JSONObject(new String(data));
        assertFalse(dataObject.isEmpty());

        var result = dataObject.optJSONObject("result");
        assertFalse(result.isEmpty());

        var dataStr = result.getString("data");
        assertFalse(dataStr.isEmpty());
        assertEquals("d5507204-465d-40f3-a114-be85ea3fafcd", dataStr);
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

            System.out.println("CLIENT Timestamp: " + instant.toString() + " CLIENT Framesize: " + frameSize + "\n");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //System.out.println("Parsed data: \n" + dataObject);
    }

    @Test
    void TestVideoFrame() {
        String systemId = "urn:osh:sensor:ffmpeg:video001"; //ffmpeg driver system id

        TestSystem(systemId, SystemType.FRAME, testNumFrames);
    }

    @Test
    void TestGeneric() {
        String systemId = "urn:osh:template_driver:sensor001";

        TestSystem(systemId, SystemType.GENERIC, testNumGenericDataPoints);
    }

    @Test
    void TestWeather() {
        String systemId = "urn:osh:sensor:simweather:0123456879"; //weather driver system id

        TestSystem(systemId, SystemType.WEATHER, testNumWeatherCoordinates);
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
                        //return node != null ? node.discoverSystems() : null;
                    } catch ( ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(systemsResult -> {
                    if (systemsResult == null || systemsResult.isEmpty()) return;

                    var dataStreamManager = oshConnect.getDataStreamManager();
                    StreamHandler streamHandler = dataStreamManager.createDataStreamHandler(( args ) -> {

                        if ( sampleCount.get() > 0 ) {

                            switch (systemType) {
                                case FRAME -> validateFrame(args.getData());
                                case GENERIC -> validateGenericData(args.getData());
                                case WEATHER -> validateWeatherData(args.getData());
                            }

                            sampleCount.set(sampleCount.get()-1);
                        }

                    });

                    switch (systemType) {
                        case FRAME -> streamHandler.setRequestFormat(RequestFormat.SWE_BINARY);
                        case GENERIC -> streamHandler.setRequestFormat(RequestFormat.OM_JSON);
                        case WEATHER -> streamHandler.setRequestFormat(RequestFormat.OM_JSON);
                    }

                    OSHSystem cSystem = systemsResult.get(0);
                    assert cSystem.getSystemResource().getUniqueIdentifier().equals(systemId);

                    System.out.println("OSHSystemID OpenSensorHub System Found. ID: " + cSystem.getId());

                    try {
                        var dataStreams = cSystem.discoverDataStreams();

                        for ( var dStream : dataStreams ) {
                            assert dStream != null;
                            streamHandler.addDataStreamListener(dStream);
                        }

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
