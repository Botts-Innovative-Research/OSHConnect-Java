package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.net.websocket.DataStreamEventArgs;
import org.sensorhub.oshconnect.net.websocket.DataStreamHandler;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataStreamManagerTest {
    DataStreamManager dataStreamManager;
    Consumer<DataStreamEventArgs> emptyEvent = args -> {
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
        DataStreamHandler handler = new DataStreamHandler() {
            @Override
            public void onStreamUpdate(DataStreamEventArgs args) {
                // Do nothing
            }
        };
        assertEquals(0, dataStreamManager.getDataStreamHandlers().size());
        dataStreamManager.addDataStreamHandler(handler);
        assertEquals(1, dataStreamManager.getDataStreamHandlers().size());
    }

    @Test
    void shutdownDataStreamHandler() {
        DataStreamHandler handler = dataStreamManager.createDataStreamHandler(emptyEvent);
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
