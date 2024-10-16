package org.sensorhub.oshconnect.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.sensorhub.oshconnect.OSHConnect;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

/**
 * Implementation of {@link ConfigManager} that exports and imports the configuration of an OSHConnect
 * instance to and from a JSON file.
 */
public class ConfigManagerJson implements ConfigManager {
    private static final String DEFAULT_CONFIG_FILE = "config.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File configFile;

    public ConfigManagerJson() {
        this(new File(DEFAULT_CONFIG_FILE));
    }

    public ConfigManagerJson(File configFile) {
        this.configFile = configFile;
    }

    /**
     * Export the configuration of an OSHConnect instance to a file in JSON format.
     */
    @Override
    public void exportConfig(OSHConnect oshConnect) throws IOException {
        try (FileWriter writer = new FileWriter(configFile)) {
            exportConfig(oshConnect, writer);
        }
    }

    /**
     * Called by {@link #exportConfig(OSHConnect)} and used in unit tests.
     */
    void exportConfig(OSHConnect oshConnect, Writer writer) {
        String name = oshConnect.getName();
        List<OSHNode> nodes = oshConnect.getNodes();
        OSHConnectConfigData configData = new OSHConnectConfigData(name, nodes);

        gson.toJson(configData, writer);
    }

    /**
     * Import the configuration from a JSON file and create a new OSHConnect instance.
     */
    @Override
    public OSHConnect importConfig() throws IOException {
        try (FileReader reader = new FileReader(configFile)) {
            return importConfig(reader);
        }
    }

    /**
     * Called by {@link #importConfig()} and used in unit tests.
     */
    OSHConnect importConfig(Reader reader) {
        OSHConnectConfigData configData = gson.fromJson(reader, OSHConnectConfigData.class);
        OSHConnect oshConnect = new OSHConnect(configData.getName(), this);
        oshConnect.addNodes(configData.getNodes());
        return oshConnect;
    }

    /**
     * Import the nodes from a JSON file containing configuration data.
     * A node will not be added to the OSHConnect instance if one already exists
     * with the same unique ID or the same name and address.
     */
    @Override
    public void importNodes(OSHConnect oshConnect) throws IOException {
        try (FileReader reader = new FileReader(configFile)) {
            importNodes(oshConnect, reader);
        }
    }

    /**
     * Called by {@link #importNodes(OSHConnect)} and used in unit tests.
     */
    void importNodes(OSHConnect oshConnect, Reader reader) {
        OSHConnectConfigData configData = gson.fromJson(reader, OSHConnectConfigData.class);
        oshConnect.addNodes(configData.getNodes());
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    @Override
    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }
}
