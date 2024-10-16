package org.sensorhub.oshconnect.config;

import org.sensorhub.oshconnect.OSHConnect;

import java.io.File;
import java.io.IOException;

/**
 * Interface for reading and writing the configuration of an OSHConnect instance.
 * <p>
 * Implement this interface to export and import the configuration of an OSHConnect instance
 * to and from a file in a specific format.
 * By default, OSHConnect uses {@link ConfigManagerJson} for JSON format.
 */
public interface ConfigManager {
    /**
     * Export the configuration of an OSHConnect instance to a file.
     *
     * @param oshConnect The OSHConnect instance to export.
     * @throws IOException If an I/O error occurred while exporting the configuration.
     */
    void exportConfig(OSHConnect oshConnect) throws IOException;

    /**
     * Create a new OSHConnect instance with the configuration from a file.
     *
     * @return The OSHConnect instance with the imported configuration.
     * @throws IOException If an I/O error occurred while importing the configuration.
     */
    OSHConnect importConfig() throws IOException;

    /**
     * Import the nodes from a file containing configuration data.
     * A node will not be added to the OSHConnect instance if one already exists
     * with the same unique ID or the same name and address.
     *
     * @param oshConnect The OSHConnect instance to import the nodes into.
     * @throws IOException If an I/O error occurred while importing the nodes.
     */
    void importNodes(OSHConnect oshConnect) throws IOException;

    /**
     * Get the file that the configuration is exported to or imported from.
     *
     * @return The file that the configuration is exported to or imported from.
     */
    File getConfigFile();

    /**
     * Set the file that the configuration is exported to or imported from.
     *
     * @param configFile The file that the configuration is exported to or imported from.
     */
    void setConfigFile(File configFile);
}