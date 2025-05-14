package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.ObservationData;
import org.sensorhub.oshconnect.util.ObservationsQueryBuilder;
import org.vast.util.TimeExtent;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.sensorhub.oshconnect.tools.DataStreamTools.*;
import static org.sensorhub.oshconnect.tools.ObservationTools.*;
import static org.sensorhub.oshconnect.tools.SystemTools.newSystem;

class OSHDataStreamTest extends TestBase {
    OSHSystem system;
    OSHDataStream dataStream;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException {
        system = node.createSystem(newSystem());
        assertNotNull(system);
        dataStream = system.createDataStream(newDataStreamInfo());
        assertNotNull(dataStream);
    }

    @Test
    void updateDataStream() throws ExecutionException, InterruptedException {
        boolean success = dataStream.updateDataStream(newDataStreamInfo("Updated Name", "Updated Description"));
        assertTrue(success);
        assertEquals("Updated Name", dataStream.getDataStreamResource().getName());
        assertEquals("Updated Description", dataStream.getDataStreamResource().getDescription());
    }

    @Test
    void refreshDataStream() throws ExecutionException, InterruptedException {
        // Update the data stream outside the context of OSHConnect
        var updatedDataStream = newDataStreamInfo("Updated Name", "Updated Description");
        Integer response = system.getConnectedSystemsApiClientExtras().updateDataStream(dataStream.getId(), updatedDataStream).get();
        assertNotNull(response);
        assertEquals(204, response);

        assertEquals(DATA_STREAM_NAME, dataStream.getDataStreamResource().getName());
        assertEquals(DATA_STREAM_DESCRIPTION, dataStream.getDataStreamResource().getDescription());
        var result = dataStream.refreshDataStream();
        assertTrue(result);
        assertEquals("Updated Name", dataStream.getDataStreamResource().getName());
        assertEquals("Updated Description", dataStream.getDataStreamResource().getDescription());
    }

    @Test
    void deleteDataStream() throws ExecutionException, InterruptedException {
        boolean success = system.deleteDataStream(dataStream);
        assertTrue(success);
        var dataStreams = system.getDataStreams();
        assertTrue(dataStreams.isEmpty());
    }

    @Test
    void pushObservation() throws ExecutionException, InterruptedException {
        ObservationData observationData = newObservationData(newDataBlockWithData());
        String observationID = dataStream.pushObservation(observationData);
        assertNotNull(observationID);
    }

    @Test
    void getObservation() throws ExecutionException, InterruptedException {
        ObservationData observationData = newObservationData(newDataBlockWithData());
        String observationID = dataStream.pushObservation(observationData);
        assertNotNull(observationID);

        ObservationData observationResult = dataStream.getObservation(observationID);
        var observations = dataStream.getObservations();
        assertFalse(observations.isEmpty());
        assertTrue(observations.stream().anyMatch(obs -> obs.getId().equals(observationID)));
        verifyObservation(observationResult, observationData.getPhenomenonTime(), OBS_LAT, OBS_LON, OBS_ALT);
    }

    @Test
    void getObservations() throws ExecutionException, InterruptedException {
        ObservationData observationData1 = newObservationData(newDataBlockWithData());
        dataStream.pushObservation(observationData1);

        ObservationData observationData2 = newObservationData(newDataBlockWithData(1.0, 2.0, 3.0));
        dataStream.pushObservation(observationData2);

        ObservationData observationData3 = newObservationData(newDataBlockWithData(1.1, 2.2, 3.3));
        dataStream.pushObservation(observationData3);

        List<ObservationData> observations = dataStream.getObservations();
        assertEquals(3, observations.size());
        verifyObservation(observations.get(0), observationData1.getPhenomenonTime(), OBS_LAT, OBS_LON, OBS_ALT);
        verifyObservation(observations.get(1), observationData2.getPhenomenonTime(), 1.0, 2.0, 3.0);
        verifyObservation(observations.get(2), observationData3.getPhenomenonTime(), 1.1, 2.2, 3.3);
    }

    @Test
    void getObservationsWithPhenomenonTime() throws ExecutionException, InterruptedException {
        ObservationData observationData1 = newObservationData(newDataBlockWithData());
        dataStream.pushObservation(observationData1);

        ObservationData observationData2 = newObservationData(newDataBlockWithData(1.0, 2.0, 3.0));
        dataStream.pushObservation(observationData2);

        ObservationData observationData3 = newObservationData(newDataBlockWithData(1.1, 2.2, 3.3));
        dataStream.pushObservation(observationData3);

        TimeExtent observation2Time = TimeExtent.period(observationData2.getPhenomenonTime(), observationData2.getPhenomenonTime());
        ObservationsQueryBuilder queryBuilder = new ObservationsQueryBuilder().phenomenonTime(observation2Time);
        List<ObservationData> observations = dataStream.getObservations(queryBuilder);
        assertEquals(1, observations.size());
        verifyObservation(observations.get(0), observationData2.getPhenomenonTime(), 1.0, 2.0, 3.0);
    }

    @Test
    void getObservationsWithPhenomenonTimeRange() throws ExecutionException, InterruptedException {
        ObservationData observationData1 = newObservationData(newDataBlockWithData());
        dataStream.pushObservation(observationData1);

        ObservationData observationData2 = newObservationData(newDataBlockWithData(1.0, 2.0, 3.0));
        dataStream.pushObservation(observationData2);

        ObservationData observationData3 = newObservationData(newDataBlockWithData(1.1, 2.2, 3.3));
        dataStream.pushObservation(observationData3);

        TimeExtent observation2Time = TimeExtent.beginAt(observationData2.getPhenomenonTime());
        ObservationsQueryBuilder queryBuilder = new ObservationsQueryBuilder().phenomenonTime(observation2Time);
        List<ObservationData> observations = dataStream.getObservations(queryBuilder);
        assertEquals(2, observations.size());
        verifyObservation(observations.get(0), observationData2.getPhenomenonTime(), 1.0, 2.0, 3.0);
        verifyObservation(observations.get(1), observationData3.getPhenomenonTime(), 1.1, 2.2, 3.3);
    }

    @Test
    void getObservationsWithLimit() throws ExecutionException, InterruptedException {
        int numObservations = 25;
        int limit = 10;
        for (int i = 0; i < numObservations; i++) {
            dataStream.pushObservation(newObservationData(newDataBlockWithData()));
        }

        ObservationsQueryBuilder queryBuilder = new ObservationsQueryBuilder().limit(limit);
        List<ObservationData> observations = dataStream.getObservations(queryBuilder);
        assertEquals(limit, observations.size());
    }
}
