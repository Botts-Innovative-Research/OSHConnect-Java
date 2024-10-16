package org.sensorhub.oshconnect.config;

import static org.junit.jupiter.api.Assertions.fail;
import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.OSH_CONNECT_NAME;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.OSHConnect;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

class ConfigManagerJsonTest {
    @Test
    void exportConfig() {
        ConfigManagerJson configManagerJson = new ConfigManagerJson();
        OSHConnect oshConnect = new OSHConnect(OSH_CONNECT_NAME, configManagerJson);
        OSHNode node = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE);
        node.setName("Node 1");
        oshConnect.addNode(node);

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(oshConnect, writer);
            assert !writer.toString().isEmpty();

            JSONObject jsonObject = new JSONObject(writer.toString());
            assert jsonObject.getString("name").equals(OSH_CONNECT_NAME);
            assert jsonObject.getJSONArray("nodes").length() == 1;
            assert jsonObject.getJSONArray("nodes").getJSONObject(0).getString("name").equals("Node 1");
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    void importConfig() {
        ConfigManagerJson configManagerJson = new ConfigManagerJson();
        OSHConnect oshConnect = new OSHConnect(OSH_CONNECT_NAME, configManagerJson);
        oshConnect.addNode(new OSHNode(SENSOR_HUB_ROOT, IS_SECURE));

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(oshConnect, writer);
            StringReader reader = new StringReader(writer.toString());

            OSHConnect importedOshConnect = configManagerJson.importConfig(reader);
            assert importedOshConnect.getName().equals(OSH_CONNECT_NAME);
            assert importedOshConnect.getNodes().size() == 1;
            reader.close();
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    void importNodes() {
        ConfigManagerJson configManagerJson = new ConfigManagerJson();
        OSHConnect oshConnect = new OSHConnect(OSH_CONNECT_NAME, configManagerJson);
        OSHNode node = new OSHNode(SENSOR_HUB_ROOT, IS_SECURE);
        UUID uniqueId = node.getUniqueId();
        oshConnect.addNode(node);

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(oshConnect, writer);
            oshConnect.removeAllNodes();

            StringReader reader = new StringReader(writer.toString());
            configManagerJson.importNodes(oshConnect, reader);
            assert oshConnect.getNodes().size() == 1;
            assert oshConnect.getNodes().get(0).getUniqueId().equals(uniqueId);
            reader.close();
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }

    @Test
    void importNodes_Duplicate() {
        ConfigManagerJson configManagerJson = new ConfigManagerJson();
        OSHConnect oshConnect = new OSHConnect(OSH_CONNECT_NAME, configManagerJson);
        oshConnect.addNode(new OSHNode(SENSOR_HUB_ROOT, IS_SECURE));

        try (StringWriter writer = new StringWriter()) {
            configManagerJson.exportConfig(oshConnect, writer);

            oshConnect.addNode(new OSHNode(SENSOR_HUB_ROOT, IS_SECURE));
            assert oshConnect.getNodes().size() == 1;

            StringReader reader = new StringReader(writer.toString());
            configManagerJson.importNodes(oshConnect, reader);
            assert oshConnect.getNodes().size() == 1;
            reader.close();
        } catch (Exception e) {
            fail("Exception thrown: " + e);
        }
    }
}