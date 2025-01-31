package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.datamodels.CommandData;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.sensorhub.oshconnect.tools.CommandTools.newCommandData;
import static org.sensorhub.oshconnect.tools.CommandTools.newDataBlockWithData;
import static org.sensorhub.oshconnect.tools.ControlStreamTools.*;
import static org.sensorhub.oshconnect.tools.SystemTools.newSystem;

class OSHControlStreamTest extends TestBase {
    OSHSystem system;
    OSHControlStream controlStream;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException {
        system = node.createSystem(newSystem());
        assertNotNull(system);
        controlStream = system.createControlStream(newCommandStreamInfo());
        assertNotNull(controlStream);
    }

    @Test
    void createControlStream() {
        assertNotNull(controlStream);
        assertNotNull(controlStream.getId());

        var commandStreamInfo = controlStream.getControlStreamResource();
        assertNotNull(commandStreamInfo);
        assertEquals(CONTROL_STREAM_NAME, commandStreamInfo.getName());
        assertEquals(CONTROL_STREAM_DESCRIPTION, commandStreamInfo.getDescription());
        assertEquals(CONTROL_RECORD_NAME, commandStreamInfo.getControlInputName());

        var recordStructure = commandStreamInfo.getRecordStructure();
        assertNotNull(recordStructure);
        assertEquals(CONTROL_RECORD_NAME, recordStructure.getName());
        assertEquals(CONTROL_RECORD_LABEL, recordStructure.getLabel());
        assertEquals(CONTROL_RECORD_DESCRIPTION, recordStructure.getDescription());
        assertEquals(4, recordStructure.getComponentCount());

        var timeField = recordStructure.getComponent(0);
        assertNotNull(timeField);
        assertEquals(TIME_FIELD_NAME, timeField.getName());
        assertEquals(TIME_FIELD_LABEL, timeField.getLabel());
        assertEquals(TIME_FIELD_DESCRIPTION, timeField.getDescription());

        var panField = recordStructure.getComponent(1);
        assertNotNull(panField);
        assertEquals(PAN_FIELD_NAME, panField.getName());
        assertEquals(PAN_FIELD_LABEL, panField.getLabel());

        var tiltField = recordStructure.getComponent(2);
        assertNotNull(tiltField);
        assertEquals(TILT_FIELD_NAME, tiltField.getName());
        assertEquals(TILT_FIELD_LABEL, tiltField.getLabel());

        var zoomField = recordStructure.getComponent(3);
        assertNotNull(zoomField);
        assertEquals(ZOOM_FIELD_NAME, zoomField.getName());
        assertEquals(ZOOM_FIELD_LABEL, zoomField.getLabel());
    }

    // TODO: Not working; 500 error
    @Test
    void updateControlStream() throws ExecutionException, InterruptedException {
        boolean success = controlStream.updateControlStream(newCommandStreamInfo("Updated Name", "Updated Description"));
        assertTrue(success);
        assertEquals("Updated Name", controlStream.getControlStreamResource().getName());
        assertEquals("Updated Description", controlStream.getControlStreamResource().getDescription());
    }

    // TODO: Not working; 500 error
    @Test
    void refreshControlStream() throws ExecutionException, InterruptedException {
        // Update the control stream outside the context of OSHConnect
        var updatedDataStream = newCommandStreamInfo("Updated Name", "Updated Description");
        Integer response = system.getConnectedSystemsApiClientExtras().updateControlStream(controlStream.getId(), updatedDataStream).get();
        assertNotNull(response);
        assertEquals(204, response);

        assertEquals(CONTROL_STREAM_NAME, controlStream.getControlStreamResource().getName());
        assertEquals(CONTROL_STREAM_DESCRIPTION, controlStream.getControlStreamResource().getDescription());
        var result = controlStream.refreshControlStream();
        assertTrue(result);
        assertEquals("Updated Name", controlStream.getControlStreamResource().getName());
        assertEquals("Updated Description", controlStream.getControlStreamResource().getDescription());
    }

    @Test
    void deleteControlStream() throws ExecutionException, InterruptedException {
        var controlStreams = system.getControlStreams();
        assertFalse(controlStreams.isEmpty());
        boolean success = system.deleteControlStream(controlStream);
        assertTrue(success);
        controlStreams = system.getControlStreams();
        assertTrue(controlStreams.isEmpty());
    }

    // TODO: Not working; 500 error
    @Test
    void sendCommand() throws ExecutionException, InterruptedException {
        CommandData commandData = newCommandData(newDataBlockWithData());
        String commandID = controlStream.pushCommand(commandData);
        assertNotNull(commandID);
    }
}
