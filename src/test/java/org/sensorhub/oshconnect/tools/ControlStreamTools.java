package org.sensorhub.oshconnect.tools;

import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.command.CommandStreamInfo;
import org.sensorhub.api.feature.FeatureId;
import org.vast.data.TextEncodingImpl;
import org.vast.swe.SWEHelper;

public class ControlStreamTools {
    public static final String CONTROL_RECORD_NAME = "cat_sensor_control";
    public static final String CONTROL_RECORD_LABEL = "Cat Sensor Control";
    public static final String CONTROL_RECORD_DESCRIPTION = "Control for the cat sensor camera.";
    public static final String TIME_FIELD_NAME = "time";
    public static final String TIME_FIELD_LABEL = "Time";
    public static final String TIME_FIELD_DESCRIPTION = "Time of data collection";
    public static final String PAN_FIELD_NAME = "pan";
    public static final String PAN_FIELD_LABEL = "Pan";
    public static final String TILT_FIELD_NAME = "tilt";
    public static final String TILT_FIELD_LABEL = "Tilt";
    public static final String ZOOM_FIELD_NAME = "zoom";
    public static final String ZOOM_FIELD_LABEL = "Zoom";
    public static final String CONTROL_STREAM_NAME = "Cat Sensor Control Stream";
    public static final String CONTROL_STREAM_DESCRIPTION = "Control for the cat sensor camera.";

    public static DataRecord dataRecord;

    static {
        SWEHelper swe = new SWEHelper();
        dataRecord = swe.createRecord()
                .name(CONTROL_RECORD_NAME)
                .updatable(true)
                .label(CONTROL_RECORD_LABEL)
                .description(CONTROL_RECORD_DESCRIPTION)
                .addField(TIME_FIELD_NAME, swe.createTime()
                        .asSamplingTimeIsoUTC()
                        .label(TIME_FIELD_LABEL)
                        .description(TIME_FIELD_DESCRIPTION))
                .addField(PAN_FIELD_NAME, swe.createQuantity()
                        .updatable(true)
                        .label(PAN_FIELD_LABEL)
                        .value(0.0))
                .addField(TILT_FIELD_NAME, swe.createQuantity()
                        .updatable(true)
                        .label(TILT_FIELD_LABEL)
                        .value(0.0))
                .addField(ZOOM_FIELD_NAME, swe.createQuantity()
                        .updatable(true)
                        .label(ZOOM_FIELD_LABEL)
                        .value(0.0))
                .build();
    }

    public static CommandStreamInfo newCommandStreamInfo() {
        return newCommandStreamInfo(CONTROL_STREAM_NAME, CONTROL_STREAM_DESCRIPTION);
    }

    public static CommandStreamInfo newCommandStreamInfo(String name, String description) {
        return new CommandStreamInfo.Builder()
                .withSystem(FeatureId.NULL_FEATURE)
                .withName(name)
                .withDescription(description)
                .withRecordDescription(dataRecord)
                .withRecordEncoding(new TextEncodingImpl())
                .build();
    }
}
