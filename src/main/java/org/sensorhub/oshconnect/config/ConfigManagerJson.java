package org.sensorhub.oshconnect.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;

import org.sensorhub.oshconnect.OSHConnect;
import org.sensorhub.oshconnect.notification.INotificationSystem;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHSystem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of {@link ConfigManager} that exports and imports the configuration of an OSHConnect
 * instance to and from a JSON file.
 */
public class ConfigManagerJson implements ConfigManager {
    private final OSHConnect oshConnect;

    public ConfigManagerJson(OSHConnect oshConnect) {
        this.oshConnect = oshConnect;
    }

    /**
     * Export the configuration of an OSHConnect instance to a file in JSON format.
     */
    @Override
    public void exportConfig(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            exportConfig(writer);
        }
    }

    /**
     * Called by {@link #exportConfig(File)} and used in unit tests.
     */
    void exportConfig(Writer writer) {
        List<OSHNode> nodes = oshConnect.getNodeManager().getNodes();

        // Export the nodes without the notification listeners
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(OSHNode.class, (JsonSerializer<OSHNode>) (src, typeOfSrc, context) -> {
                    JsonElement jsonElement = new Gson().toJsonTree(src);
                    jsonElement.getAsJsonObject().remove("systems");
                    jsonElement.getAsJsonObject().remove("systemNotificationListeners");
                    return jsonElement;
                })
                .create();

        gson.toJson(nodes, writer);
    }

    /**
     * Import the configuration from a JSON file.
     */
    @Override
    public void importConfig(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            importConfig(reader);
        }
    }

    /**
     * Called by {@link #importConfig(File)} and used in unit tests.
     */
    void importConfig(Reader reader) {
        // Import the nodes with a new notification manager
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(OSHNode.class, (JsonDeserializer<OSHNode>) (src, typeOfSrc, context) -> {
                    JsonElement jsonElement = new Gson().toJsonTree(src);
                    // Add a new Set<INotificationSystem> to the JsonElement
                    jsonElement.getAsJsonObject().add("systems", new Gson().toJsonTree(new HashSet<OSHSystem>()));
                    jsonElement.getAsJsonObject().add("systemNotificationListeners", new Gson().toJsonTree(new HashSet<INotificationSystem>()));
                    return new Gson().fromJson(jsonElement, OSHNode.class);
                })
                .create();

        OSHNode[] nodes = gson.fromJson(reader, OSHNode[].class);
        oshConnect.getNodeManager().addNodes(List.of(nodes));
    }
}
