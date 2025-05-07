package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.util.SystemsQueryBuilder;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.sensorhub.oshconnect.tools.SystemTools.newSystem;

class OSHNodeTest extends TestBase {
    private OSHSystem system;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException {
        system = node.createSystem(newSystem());
        assertNotNull(system);
    }

    @Test
    void createSystem() {
        assertNotNull(system);
        assertNotNull(system.getSystemResource());
        System.out.println("System: " + system.getSystemResource().getGeometry());
    }

    @Test
    void discoverSystems_InsideBounds() throws ExecutionException, InterruptedException {
        var query = new SystemsQueryBuilder().bbox(34.706641, -86.737455, 34.713744, -86.731978);
        var systems = node.discoverSystems(query);
        assertNotNull(systems);
        assertEquals(1, systems.size());
    }

    @Test
    void discoverSystems_OutsideBounds() throws ExecutionException, InterruptedException {
        var query = new SystemsQueryBuilder().bbox(34.707814, -86.731416, 34.715542, -86.724928);
        var systems = node.discoverSystems(query);
        assertNotNull(systems);
        assertEquals(0, systems.size());
    }
}
