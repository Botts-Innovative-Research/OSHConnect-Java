package org.sensorhub.oshconnect.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.OSH_CONNECT_NAME;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.NodeManager;
import org.sensorhub.oshconnect.OSHConnect;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

class ConfigManagerJsonTest {
    private OSHConnect oshConnect;
    ConfigManagerJson configManagerJson;
    private NodeManager nodeManager;

    @BeforeEach
    void setUp() {
        configManagerJson = new ConfigManagerJson();
        oshConnect = new OSHConnect(OSH_CONNECT_NAME, configManagerJson);
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
            configManagerJson.exportConfig(oshConnect, writer);
            assert !writer.toString().isEmpty();

            JSONObject jsonObject = new JSONObject(writer.toString());
            assertEquals(OSH_CONNECT_NAME, jsonObject.getString("name"));
            assertEquals(1, jsonObject.getJSONArray("nodes").length());
            assertEquals("Node 1", jsonObject.getJSONArray("nodes").getJSONObject(0).getString("name"));
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    void importConfig() {
        oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(oshConnect, writer);
            StringReader reader = new StringReader(writer.toString());

            OSHConnect importedOshConnect = configManagerJson.importConfig(reader);

            assertEquals(OSH_CONNECT_NAME, importedOshConnect.getName());
            assertEquals(1, importedOshConnect.getNodeManager().getNodes().size());
            reader.close();
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    void importNodes() {
        OSHNode node = oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        UUID uniqueId = node.getUniqueId();

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(oshConnect, writer);
            nodeManager.removeAllNodes();

            StringReader reader = new StringReader(writer.toString());
            configManagerJson.importNodes(oshConnect, reader);
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
            configManagerJson.exportConfig(oshConnect, writer);

            StringReader reader = new StringReader(writer.toString());
            configManagerJson.importNodes(oshConnect, reader);
            assertEquals(1, nodeManager.getNodes().size());
            reader.close();
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }
}