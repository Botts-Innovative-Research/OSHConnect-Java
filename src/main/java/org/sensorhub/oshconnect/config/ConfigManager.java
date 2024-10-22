package org.sensorhub.oshconnect.config;

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
     * @param file The file to export the configuration to.
     * @throws IOException If an I/O error occurred while exporting the configuration.
     */
    void exportConfig(File file) throws IOException;

    /**
     * Create a new OSHConnect instance with the configuration from a file.
     *
     * @param file The file to import the configuration from.
     * @throws IOException If an I/O error occurred while importing the configuration.
     */
    void importConfig(File file) throws IOException;
}