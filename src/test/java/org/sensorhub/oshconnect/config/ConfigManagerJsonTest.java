package org.sensorhub.oshconnect.config;

import org.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.NodeManager;
import org.sensorhub.oshconnect.OSHConnect;
import org.sensorhub.oshconnect.OSHNode;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.sensorhub.oshconnect.TestConstants.*;

class ConfigManagerJsonTest {
    private OSHConnect oshConnect;
    private ConfigManagerJson configManagerJson;
    private NodeManager nodeManager;

    @BeforeEach
    void setUp() {
        oshConnect = new OSHConnect(OSH_CONNECT_NAME);
        configManagerJson = new ConfigManagerJson(oshConnect);
        oshConnect.setConfigManager(configManagerJson);
        nodeManager = oshConnect.getNodeManager();
    }

    @AfterEach
    void tearDown() {
        oshConnect.shutdown();
    }

    @Test
    void exportConfig() {
        OSHNode node = oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        node.setName("Node 1");

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(writer);
            assert !writer.toString().isEmpty();

            JSONArray jsonArray = new JSONArray(writer.toString());
            assertEquals(1, jsonArray.length());
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    void importConfig() {
        OSHNode node = oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        UUID uniqueId = node.getUniqueId();

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(writer);
            nodeManager.removeAllNodes();

            StringReader reader = new StringReader(writer.toString());
            configManagerJson.importConfig(reader);
            assertEquals(1, nodeManager.getNodes().size());
            assertEquals(uniqueId, nodeManager.getNodes().get(0).getUniqueId());
            reader.close();
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    void importNodes_Duplicate() {
        oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        assertEquals(1, nodeManager.getNodes().size());

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(writer);

            StringReader reader = new StringReader(writer.toString());
            configManagerJson.importConfig(reader);
            assertEquals(1, nodeManager.getNodes().size());
            reader.close();
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }
}