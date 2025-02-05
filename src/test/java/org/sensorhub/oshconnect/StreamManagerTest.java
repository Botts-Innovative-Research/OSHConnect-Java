package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.net.websocket.StreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.StreamHandler;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StreamManagerTest {
    StreamManager dataStreamManager;
    Consumer<StreamEventArgs> emptyEvent = args -> {
        // Do nothing
    };

    @BeforeEach
    void setup() {
        var oshConnect = new OSHConnect();
        dataStreamManager = oshConnect.getDataStreamManager();
    }

    @Test
    void createDataStreamHandler() {
        dataStreamManager.createDataStreamHandler(emptyEvent);
        assertEquals(1, dataStreamManager.getDataStreamHandlers().size());
    }

    @Test
    void addDataStreamHandler() {
        StreamHandler handler = new StreamHandler() {
            @Override
            public void onStreamUpdate(StreamEventArgs args) {
                // Do nothing
            }
        };
        assertEquals(0, dataStreamManager.getDataStreamHandlers().size());
        dataStreamManager.addDataStreamHandler(handler);
        assertEquals(1, dataStreamManager.getDataStreamHandlers().size());
    }

    @Test
    void shutdownDataStreamHandler() {
        StreamHandler handler = dataStreamManager.createDataStreamHandler(emptyEvent);
        assertEquals(1, dataStreamManager.getDataStreamHandlers().size());
        dataStreamManager.shutdownDataStreamHandler(handler);
        assertEquals(0, dataStreamManager.getDataStreamHandlers().size());
        assertThrows(IllegalStateException.class, handler::connect);
    }

    @Test
    void shutdownDataStreamHandlers() {
        dataStreamManager.createDataStreamHandler(emptyEvent);
        dataStreamManager.createDataStreamHandler(emptyEvent);
        assertEquals(2, dataStreamManager.getDataStreamHandlers().size());
        dataStreamManager.shutdownDataStreamHandlers();
        assertEquals(0, dataStreamManager.getDataStreamHandlers().size());
    }
}
