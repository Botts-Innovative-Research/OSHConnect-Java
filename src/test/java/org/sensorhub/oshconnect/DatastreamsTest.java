package org.sensorhub.oshconnect;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class DatastreamsTest extends TestBase {
    @Test
    void testCreateDatastream() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        var datastream = system.createDatastream(newDataStreamInfo());
        assertNotNull(datastream);
        assertNotNull(datastream.getDatastreamResource());
    }

    @Test
    void testUpdateDatastream() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        var datastream = system.createDatastream(newDataStreamInfo());
        assertNotNull(datastream);
        boolean success = datastream.updateDatastream(newDataStreamInfo("Updated Name", "Updated Description"));
        assertTrue(success);
        assertEquals("Updated Name", datastream.getDatastreamResource().getName());
        assertEquals("Updated Description", datastream.getDatastreamResource().getDescription());
    }

    @Test
    void testDeleteDatastream() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        var datastream = system.createDatastream(newDataStreamInfo());
        assertNotNull(datastream);
        boolean success = system.deleteDatastream(datastream);
        assertTrue(success);
        var datastreams = system.getDatastreams();
        assertTrue(datastreams.isEmpty());
    }
}
