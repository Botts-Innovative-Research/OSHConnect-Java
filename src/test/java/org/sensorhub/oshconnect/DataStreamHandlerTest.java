package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.net.websocket.DataStreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DataStreamHandler;
import org.sensorhub.oshconnect.net.websocket.StreamStatus;
import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.sensorhub.oshconnect.tools.DataStreamTools.newDataStreamInfo;
import static org.sensorhub.oshconnect.tools.SystemTools.newSystem;

class DataStreamHandlerTest extends TestBase {
    OSHSystem system;
    OSHDataStream dataStream;
    DataStreamManager dataStreamManager;
    DataStreamHandler dataStreamHandler;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException {
        dataStreamManager = oshConnect.getDataStreamManager();
        system = node.createSystem(newSystem());
        assertNotNull(system);
        dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);
        dataStreamHandler = dataStreamManager.createDataStreamHandler(args -> {
            // Do nothing
        });
        assertNotNull(dataStreamHandler);
    }

    @Test
    void dataStreamHandler() {
        assertNotNull(dataStreamHandler);
        assertEquals(0, dataStreamHandler.getDataStreamListeners().size());
        assertEquals(StreamStatus.DISCONNECTED, dataStreamHandler.getStatus());
        assertNull(dataStreamHandler.getRequestFormat());
        assertNull(dataStreamHandler.getTimeExtent());
        assertEquals(1, dataStreamHandler.getReplaySpeed());
    }

    @Test
    void connect() {
        dataStreamHandler.connect();
        assertEquals(StreamStatus.CONNECTED, dataStreamHandler.getStatus());
    }

    @Test
    void disconnect() {
        dataStreamHandler.connect();
        assertEquals(StreamStatus.CONNECTED, dataStreamHandler.getStatus());
        dataStreamHandler.disconnect();
        assertEquals(StreamStatus.DISCONNECTED, dataStreamHandler.getStatus());
    }

    @Test
    void shutdown() {
        dataStreamHandler.connect();
        assertEquals(StreamStatus.CONNECTED, dataStreamHandler.getStatus());
        dataStreamHandler.shutdown();
        assertEquals(StreamStatus.SHUTDOWN, dataStreamHandler.getStatus());
    }

    @Test
    void connect_AfterShutdown() {
        dataStreamHandler.connect();
        assertEquals(StreamStatus.CONNECTED, dataStreamHandler.getStatus());
        dataStreamHandler.shutdown();
        assertEquals(StreamStatus.SHUTDOWN, dataStreamHandler.getStatus());
        assertThrows(IllegalStateException.class, dataStreamHandler::connect);
    }

    @Test
    void addDataStreamListener() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
    }

    @Test
    void addDataStreamListener_Duplicate() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        var dataStreamListener2 = dataStreamHandler.addDataStreamListener(dataStream);
        assertEquals(dataStreamListener, dataStreamListener2);
    }

    @Test
    void addDataStreamListener_Multiple() throws ExecutionException, InterruptedException {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        var dataStream2 = system.createDataStream(newDataStreamInfo("dataStream2", "dataStream2"));
        var dataStreamListener2 = dataStreamHandler.addDataStreamListener(dataStream2);
        assertNotNull(dataStreamListener2);
        assertNotEquals(dataStreamListener, dataStreamListener2);
    }

    @Test
    void addDataStreamListener_Null() {
        assertThrows(IllegalArgumentException.class, () -> dataStreamHandler.addDataStreamListener(null));
    }

    @Test
    void getDataStreamListeners() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        var dataStreamListeners = dataStreamHandler.getDataStreamListeners();
        assertNotNull(dataStreamListeners);
        assertEquals(1, dataStreamListeners.size());
        assertEquals(dataStreamListener, dataStreamListeners.get(0));
    }

    @Test
    void getDataStreamListeners_Empty() {
        var dataStreamListeners = dataStreamHandler.getDataStreamListeners();
        assertEquals(0, dataStreamListeners.size());
    }

    @Test
    void getDataStreamListeners_Multiple() throws ExecutionException, InterruptedException {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        var dataStream2 = system.createDataStream(newDataStreamInfo("dataStream2", "dataStream2"));
        var dataStreamListener2 = dataStreamHandler.addDataStreamListener(dataStream2);
        assertNotNull(dataStreamListener2);
        var dataStreamListeners = dataStreamHandler.getDataStreamListeners();
        assertEquals(2, dataStreamListeners.size());
        assertTrue(dataStreamListeners.contains(dataStreamListener));
        assertTrue(dataStreamListeners.contains(dataStreamListener2));
    }

    @Test
    void shutdownDataStreamListener_OSHDataStream() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        var didShutdown = dataStreamHandler.shutdownDataStreamListener(dataStream);
        assertTrue(didShutdown);
        var dataStreamListeners = dataStreamHandler.getDataStreamListeners();
        assertEquals(0, dataStreamListeners.size());
        assertEquals(StreamStatus.SHUTDOWN, dataStreamListener.getStatus());
    }

    @Test
    void shutdownDataStreamListener_OSHDataStream_DoesNotExistInHandler() throws ExecutionException, InterruptedException {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        var dataStream2 = system.createDataStream(newDataStreamInfo("dataStream2", "dataStream2"));
        var didShutdown = dataStreamHandler.shutdownDataStreamListener(dataStream2);
        assertFalse(didShutdown);
        var dataStreamListeners = dataStreamHandler.getDataStreamListeners();
        assertEquals(1, dataStreamListeners.size());
        assertEquals(dataStreamListener, dataStreamListeners.get(0));
    }

    @Test
    void shutdownDataStreamListener_DataStreamListener() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        var didShutdown = dataStreamHandler.shutdownDataStreamListener(dataStreamListener);
        assertTrue(didShutdown);
        var dataStreamListeners = dataStreamHandler.getDataStreamListeners();
        assertEquals(0, dataStreamListeners.size());
        assertEquals(StreamStatus.SHUTDOWN, dataStreamListener.getStatus());
    }

    @Test
    void shutdownDataStreamListener_DataStreamListener_DoesNotExistInHandler() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);

        var dataStreamHandler2 = new DataStreamHandler() {
            @Override
            public void onStreamUpdate(DataStreamEventArgs args) {
                // Do nothing
            }
        };
        assertNotNull(dataStreamHandler2);
        var dataStreamListener2 = dataStreamHandler2.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener2);

        var didShutdown = dataStreamHandler.shutdownDataStreamListener(dataStreamListener2);
        assertFalse(didShutdown);
        assertNotEquals(StreamStatus.SHUTDOWN, dataStreamListener2.getStatus());
        var dataStreamListeners = dataStreamHandler.getDataStreamListeners();
        assertEquals(1, dataStreamListeners.size());
        assertEquals(dataStreamListener, dataStreamListeners.get(0));
    }

    @Test
    void shutdownAllDataStreamListeners() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        dataStreamHandler.shutdownAllDataStreamListeners();
        var dataStreamListeners = dataStreamHandler.getDataStreamListeners();
        assertEquals(0, dataStreamListeners.size());
        assertEquals(StreamStatus.SHUTDOWN, dataStreamListener.getStatus());
    }

    @Test
    void setRequestFormat() {
        dataStreamHandler.setRequestFormat(RequestFormat.SWE_BINARY);
        assertEquals(RequestFormat.SWE_BINARY, dataStreamHandler.getRequestFormat());
    }

    @Test
    void setRequestFormat_ListenerAddedBefore() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        dataStreamHandler.setRequestFormat(RequestFormat.JSON);
        assertEquals(RequestFormat.JSON, dataStreamHandler.getRequestFormat());
        assertEquals(RequestFormat.JSON, dataStreamListener.getRequestFormat());
    }

    @Test
    void setRequestFormat_ListenerAddedAfter() {
        dataStreamHandler.setRequestFormat(RequestFormat.SWE_XML);
        assertEquals(RequestFormat.SWE_XML, dataStreamHandler.getRequestFormat());
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(RequestFormat.SWE_XML, dataStreamListener.getRequestFormat());
    }

    @Test
    void setReplaySpeed() {
        dataStreamHandler.setReplaySpeed(1.0);
        assertEquals(1.0, dataStreamHandler.getReplaySpeed());
    }

    @Test
    void setReplaySpeed_ListenerAddedBefore() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        dataStreamHandler.setReplaySpeed(2.0);
        assertEquals(2.0, dataStreamHandler.getReplaySpeed());
        assertEquals(2.0, dataStreamListener.getReplaySpeed());
    }

    @Test
    void setReplaySpeed_ListenerAddedAfter() {
        dataStreamHandler.setReplaySpeed(3.0);
        assertEquals(3.0, dataStreamHandler.getReplaySpeed());
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(3.0, dataStreamListener.getReplaySpeed());
    }

    @Test
    void setTimeExtent() {
        var timeExtent = TimeExtent.beginAt(Instant.now());
        dataStreamHandler.setTimeExtent(timeExtent);
        assertEquals(timeExtent, dataStreamHandler.getTimeExtent());
    }

    @Test
    void setTimeExtent_ListenerAddedBefore() {
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        var timeExtent = TimeExtent.beginAt(Instant.now());
        dataStreamHandler.setTimeExtent(timeExtent);
        assertEquals(timeExtent, dataStreamHandler.getTimeExtent());
        assertEquals(timeExtent, dataStreamListener.getTimeExtent());
    }

    @Test
    void setTimeExtent_ListenerAddedAfter() {
        var timeExtent = TimeExtent.beginAt(Instant.now());
        dataStreamHandler.setTimeExtent(timeExtent);
        assertEquals(timeExtent, dataStreamHandler.getTimeExtent());
        var dataStreamListener = dataStreamHandler.addDataStreamListener(dataStream);
        assertNotNull(dataStreamListener);
        assertEquals(timeExtent, dataStreamListener.getTimeExtent());
    }
}
