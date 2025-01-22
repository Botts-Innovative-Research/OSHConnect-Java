package org.sensorhub.oshconnect.tools;

import net.opengis.swe.v20.DataBlock;
import org.sensorhub.oshconnect.datamodels.ObservationData;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.sensorhub.oshconnect.tools.DataStreamTools.dataRecord;

public class ObservationTools {
    public static final double OBS_LAT = 1.5;
    public static final double OBS_LON = 2.5;
    public static final double OBS_ALT = 3.5;

    public static final int OBS_TIME_INDEX = 0;
    public static final int OBS_LAT_INDEX = 1;
    public static final int OBS_LON_INDEX = 2;
    public static final int OBS_ALT_INDEX = 3;

    public static void verifyObservation(ObservationData observationData, Instant expectedPhenomenonTime, double expectedLat, double expectedLon, double expectedAlt) {
        assertNotNull(observationData);
        DataBlock dataBlock = observationData.getResult();
        assertNotNull(dataBlock);

        assertEquals(expectedPhenomenonTime, observationData.getPhenomenonTime());
        assertEquals(expectedLat, dataBlock.getDoubleValue(OBS_LAT_INDEX));
        assertEquals(expectedLon, dataBlock.getDoubleValue(OBS_LON_INDEX));
        assertEquals(expectedAlt, dataBlock.getDoubleValue(OBS_ALT_INDEX));
    }

    public static ObservationData newObservationData(DataBlock dataBlock) {
        return newObservationData(dataBlock, Instant.now());
    }

    public static ObservationData newObservationData(DataBlock dataBlock, Instant phenomenonTime) {
        return ObservationData.newBuilder()
                .phenomenonTime(phenomenonTime)
                .result(dataBlock)
                .build();
    }

    public static DataBlock newDataBlockWithData() {
        return newDataBlockWithData(OBS_LAT, OBS_LON, OBS_ALT);
    }

    public static DataBlock newDataBlockWithData(double lat, double lon, double alt) {
        DataBlock dataBlock = dataRecord.createDataBlock();
        dataBlock.setDoubleValue(OBS_TIME_INDEX, System.currentTimeMillis() / 1000d);
        dataBlock.setDoubleValue(OBS_LAT_INDEX, lat);
        dataBlock.setDoubleValue(OBS_LON_INDEX, lon);
        dataBlock.setDoubleValue(OBS_ALT_INDEX, alt);
        return dataBlock;
    }
}
