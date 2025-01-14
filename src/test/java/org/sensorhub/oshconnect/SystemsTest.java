package org.sensorhub.oshconnect;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class SystemsTest extends TestBase {
    @Test
    void testCreateSystem() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        assertNotNull(system);
        assertNotNull(system.getSystemResource());
    }

    @Test
    void testUpdateSystem() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        assertNotNull(system);
        var result = system.updateSystem(newSystem("Updated Name", "Updated Description"));
        assertTrue(result);
        assertEquals("Updated Name", system.getSystemResource().getName());
        assertEquals("Updated Description", system.getSystemResource().getDescription());
    }

    @Test
    void testDeleteSystem() throws ExecutionException, InterruptedException {
        var system = node.createSystem(newSystem());
        assertNotNull(system);
        var result = node.deleteSystem(system);
        assertTrue(result);
        var systems = node.getSystems();
        assertNotNull(systems);
        assertFalse(systems.stream().anyMatch(s -> s.getSystemResource().getId().equals(system.getSystemResource().getId())));
    }
}
