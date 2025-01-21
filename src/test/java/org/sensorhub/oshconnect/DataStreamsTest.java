package org.sensorhub.oshconnect;

import net.opengis.swe.v20.DataBlock;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.ObservationData;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class DataStreamsTest extends TestBase {
    protected static final double OBS_LAT = 1.5;
    protected static final double OBS_LON = 2.5;
    protected static final double OBS_ALT = 3.5;

    protected static final int OBS_TIME_INDEX = 0;
    protected static final int OBS_LAT_INDEX = 1;
    protected static final int OBS_LON_INDEX = 2;
    protected static final int OBS_ALT_INDEX = 3;

    @Test
    void testCreateDataStream() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        var dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);
        assertNotNull(dataStream.getDataStreamResource());
    }

    @Test
    void testUpdateDataStream() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        var dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);
        boolean success = dataStream.updateDataStream(newDataStreamInfo("Updated Name", "Updated Description"));
        assertTrue(success);
        assertEquals("Updated Name", dataStream.getDataStreamResource().getName());
        assertEquals("Updated Description", dataStream.getDataStreamResource().getDescription());
    }

    @Test
    void testDeleteDataStream() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        var dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);
        boolean success = system.deleteDataStream(dataStream);
        assertTrue(success);
        var dataStreams = system.getDataStreams();
        assertTrue(dataStreams.isEmpty());
    }

    @Test
    void testPushObservation() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        var dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);

        ObservationData observationData = ObservationData.newBuilder()
                .phenomenonTime(Instant.now())
                .result(newDataBlockWithData())
                .build();
        String observationID = dataStream.pushObservation(observationData);
        assertNotNull(observationID);

        ObservationData observationResult = dataStream.getObservation(observationID);
        assertNotNull(observationResult);
        DataBlock dataBlock = observationResult.getResult();
        assertNotNull(dataBlock);

        assertEquals(observationData.getPhenomenonTime(), observationResult.getPhenomenonTime());
        assertEquals(OBS_LAT, dataBlock.getDoubleValue(OBS_LAT_INDEX));
        assertEquals(OBS_LON, dataBlock.getDoubleValue(OBS_LON_INDEX));
        assertEquals(OBS_ALT, dataBlock.getDoubleValue(OBS_ALT_INDEX));

        var observations = dataStream.getObservations();
        assertFalse(observations.isEmpty());
        assertTrue(observations.stream().anyMatch(obs -> obs.getId().equals(observationID)));
    }

    protected DataBlock newDataBlockWithData() {
        return newDataBlockWithData(OBS_LAT, OBS_LON, OBS_ALT);
    }

    protected DataBlock newDataBlockWithData(double lat, double lon, double alt) {
        DataBlock dataBlock = dataRecord.createDataBlock();
        dataBlock.setDoubleValue(OBS_TIME_INDEX, System.currentTimeMillis() / 1000d);
        dataBlock.setDoubleValue(OBS_LAT_INDEX, lat);
        dataBlock.setDoubleValue(OBS_LON_INDEX, lon);
        dataBlock.setDoubleValue(OBS_ALT_INDEX, alt);
        return dataBlock;
    }
}
