package org.sensorhub.oshconnect;

import org.junit.jupiter.api.Test;
import org.sensorhub.impl.service.consys.sensorml.SystemAdapter;
import org.vast.sensorML.SMLHelper;

import static org.junit.jupiter.api.Assertions.*;

class SystemsTest extends TestBase {
    @Test
    void testCreateSystem() {
        var system = node.createSystem(newSystem());
        assertNotNull(system);
    }

    @Test
    void testUpdateSystem() {
        var system = node.createSystem(newSystem());
        assertNotNull(system);
        system.updateSystem(newSystem("Updated Cat Sensor", "An updated sensor that measures the number of cats in the room."));
        assertEquals("Updated Cat Sensor", system.getSystemResource().getName());
        assertEquals("An updated sensor that measures the number of cats in the room.", system.getSystemResource().getDescription());
    }

    @Test
    void testDeleteSystem() {
        var system = node.createSystem(newSystem());
        assertNotNull(system);
        node.deleteSystem(system);
        var systems = node.getSystems();
        assertNotNull(systems);
        assertFalse(systems.stream().anyMatch(s -> s.getSystemResource().getId().equals(system.getSystemResource().getId())));
    }

    private SystemAdapter newSystem() {
        return newSystem("Cat Sensor", "A sensor that measures the number of cats in the room.");
    }

    private SystemAdapter newSystem(String name, String description) {
        var sys = new SMLHelper().createPhysicalSystem()
                .uniqueID("urn:sensor:cat_sensor_001")
                .name(name)
                .description(description)
                .build();


        return new SystemAdapter(sys);
    }
}
