package org.sensorhub.oshconnect.tools;

import net.opengis.swe.v20.DataBlock;
import org.sensorhub.oshconnect.datamodels.CommandData;

public class CommandTools {
    public static final double COMMAND_PAN = 1.5;
    public static final double COMMAND_TILT = 2.5;
    public static final double COMMAND_ZOOM = 3.5;

    public static final int COMMAND_TIME_INDEX = 0;
    public static final int COMMAND_PAN_INDEX = 1;
    public static final int COMMAND_TILT_INDEX = 2;
    public static final int COMMAND_ZOOM_INDEX = 3;

    public static CommandData newCommandData(DataBlock dataBlock) {
        return CommandData.newBuilder()
                .parameters(dataBlock)
                .build();
    }

    public static DataBlock newDataBlockWithData() {
        return newDataBlockWithData(COMMAND_PAN, COMMAND_TILT, COMMAND_ZOOM);
    }

    public static DataBlock newDataBlockWithData(double pan, double tilt, double zoom) {
        DataBlock dataBlock = ControlStreamTools.dataRecord.createDataBlock();
        dataBlock.setDoubleValue(COMMAND_TIME_INDEX, System.currentTimeMillis() / 1000d);
        dataBlock.setDoubleValue(COMMAND_PAN_INDEX, pan);
        dataBlock.setDoubleValue(COMMAND_TILT_INDEX, tilt);
        dataBlock.setDoubleValue(COMMAND_ZOOM_INDEX, zoom);
        return dataBlock;
    }
}
