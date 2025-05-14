package org.sensorhub.oshconnect.tools;

import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.feature.FeatureId;
import org.vast.data.TextEncodingImpl;
import org.vast.swe.helper.GeoPosHelper;

public class DataStreamTools {
    public static final String DATA_RECORD_NAME = "cat_sensor_data";
    public static final String DATA_RECORD_LABEL = "Cat Sensor Data";
    public static final String DATA_RECORD_DESCRIPTION = "Position data from the cat sensor.";
    public static final String TIME_FIELD_NAME = "time";
    public static final String TIME_FIELD_LABEL = "Time";
    public static final String TIME_FIELD_DESCRIPTION = "Time of data collection";
    public static final String POSITION_FIELD_NAME = "pos";
    public static final String POSITION_FIELD_LABEL = "Position";
    public static final String DATA_STREAM_NAME = "Cat Sensor Datastream";
    public static final String DATA_STREAM_DESCRIPTION = "Position of cats in the room.";

    public static DataRecord dataRecord;

    static {
        var swe = new GeoPosHelper();
        dataRecord = swe.createRecord()
                .name(DATA_RECORD_NAME)
                .label(DATA_RECORD_LABEL)
                .description(DATA_RECORD_DESCRIPTION)
                .addField(TIME_FIELD_NAME, swe.createTime()
                        .asSamplingTimeIsoUTC()
                        .label(TIME_FIELD_LABEL)
                        .description(TIME_FIELD_DESCRIPTION))
                .addField(POSITION_FIELD_NAME, swe.createLocationVectorLLA()
                        .label(POSITION_FIELD_LABEL))
                .build();
    }

    public static DataStreamInfo newDataStreamInfo() {
        return newDataStreamInfo(DATA_STREAM_NAME, DATA_STREAM_DESCRIPTION);
    }

    public static DataStreamInfo newDataStreamInfo(String name, String description) {
        return new DataStreamInfo.Builder()
                .withSystem(FeatureId.NULL_FEATURE)
                .withName(name)
                .withDescription(description)
                .withRecordDescription(dataRecord)
                .withRecordEncoding(new TextEncodingImpl())
                .build();
    }
}
