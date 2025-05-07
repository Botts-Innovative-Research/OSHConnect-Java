package org.sensorhub.oshconnect.tools;

import net.opengis.gml.v32.impl.GMLFactory;
import org.sensorhub.impl.service.consys.sensorml.SystemAdapter;
import org.vast.sensorML.SMLHelper;

public class SystemTools {
    public static final String SENSOR_NAME = "Cat Sensor";
    public static final String SENSOR_DESCRIPTION = "A sensor that measures cats in the room.";
    public static final String SENSOR_UID = "urn:sensor:cat_sensor_001";

    public static SystemAdapter newSystem() {
        return newSystem(SENSOR_NAME, SENSOR_DESCRIPTION);
    }

    public static SystemAdapter newSystem(String name, String description) {
        var sys = new SMLHelper().createPhysicalSystem()
                .uniqueID(SENSOR_UID)
                .name(name)
                .description(description)
                .location(new GMLFactory().newPoint(34.710127, -86.734610))
                .build();

        return new SystemAdapter(sys);
    }
}
