package org.sensorhub.oshconnect;

import net.opengis.swe.v20.DataBlock;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.ObservationData;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class DatastreamsTest extends TestBase {
    protected static final double OBS_LAT = 1.5;
    protected static final double OBS_LON = 2.5;
    protected static final double OBS_ALT = 3.5;

    protected static final int OBS_TIME_INDEX = 0;
    protected static final int OBS_LAT_INDEX = 1;
    protected static final int OBS_LON_INDEX = 2;
    protected static final int OBS_ALT_INDEX = 3;

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

    @Test
    void testPushObservation() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        var datastream = system.createDatastream(newDataStreamInfo());
        assertNotNull(datastream);

        ObservationData observationData = ObservationData.newBuilder()
                .phenomenonTime(Instant.now())
                .result(newDataBlockWithData())
                .build();
        String observationID = datastream.pushObservation(observationData);
        assertNotNull(observationID);

        ObservationData observationResult = datastream.getObservation(observationID);
        assertNotNull(observationResult);
        DataBlock dataBlock = observationResult.getResult();
        assertNotNull(dataBlock);

        assertEquals(observationData.getPhenomenonTime(), observationResult.getPhenomenonTime());
        assertEquals(OBS_LAT, dataBlock.getDoubleValue(OBS_LAT_INDEX));
        assertEquals(OBS_LON, dataBlock.getDoubleValue(OBS_LON_INDEX));
        assertEquals(OBS_ALT, dataBlock.getDoubleValue(OBS_ALT_INDEX));

        var observations = datastream.getObservationsList();
        assertFalse(observations.isEmpty());
        assertTrue(observations.stream().anyMatch(obs -> obs.getId().equals(observationID)));
    }

    protected DataBlock newDataBlockWithData() {
        DataBlock dataBlock = dataRecord.createDataBlock();
        dataBlock.setDoubleValue(OBS_TIME_INDEX, System.currentTimeMillis() / 1000d);
        dataBlock.setDoubleValue(OBS_LAT_INDEX, OBS_LAT);
        dataBlock.setDoubleValue(OBS_LON_INDEX, OBS_LON);
        dataBlock.setDoubleValue(OBS_ALT_INDEX, OBS_ALT);
        return dataBlock;
    }
}
