package org.sensorhub.oshconnect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.opengis.swe.v20.DataRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.feature.FeatureId;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleEvent;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.datastore.h2.MVObsSystemDatabaseConfig;
import org.sensorhub.impl.service.HttpServer;
import org.sensorhub.impl.service.HttpServerConfig;
import org.sensorhub.impl.service.consys.ConSysApiService;
import org.sensorhub.impl.service.consys.ConSysApiServiceConfig;
import org.sensorhub.impl.service.consys.sensorml.SystemAdapter;
import org.vast.data.TextEncodingImpl;
import org.vast.sensorML.SMLHelper;
import org.vast.swe.helper.GeoPosHelper;

import java.io.File;
import java.io.IOException;

import static org.sensorhub.oshconnect.TestConstants.*;

public class TestBase {
    static final int SERVER_PORT = 8181;
    static final long TIMEOUT = 10000;
    protected SensorHub hub;
    protected File dbFile;
    protected ConSysApiService service;
    protected IObsSystemDatabase db;
    protected String apiRootUrl;
    protected Gson gson = new GsonBuilder().setPrettyPrinting().create();
    protected OSHConnect oshConnect;
    protected OSHNode node;
    protected DataRecord dataRecord;

    @BeforeEach
    void setUp() throws IOException, SensorHubException {
        // Use a temp DB file.
        dbFile = File.createTempFile("sweapi-db-", ".dat");
        dbFile.deleteOnExit();

        // Get the instance with in-memory DB
        hub = new SensorHub();
        hub.start();
        var moduleRegistry = hub.getModuleRegistry();

        // Start the HTTP server
        HttpServerConfig httpConfig = new HttpServerConfig();
        httpConfig.httpPort = SERVER_PORT;
        var httpServer = (HttpServer) moduleRegistry.loadModule(httpConfig, TIMEOUT);

        // Start the DB
        MVObsSystemDatabaseConfig dbCfg = new MVObsSystemDatabaseConfig();
        dbCfg.storagePath = dbFile.getAbsolutePath();
        dbCfg.databaseNum = 2;
        dbCfg.readOnly = false;
        dbCfg.name = "SWE API Database";
        dbCfg.autoStart = true;
        db = (IObsSystemDatabase) moduleRegistry.loadModule(dbCfg, TIMEOUT);
        ((IModule<?>) db).waitForState(ModuleEvent.ModuleState.STARTED, TIMEOUT);

        // Start the Connected Systems API service
        ConSysApiServiceConfig swaCfg = new ConSysApiServiceConfig();
        swaCfg.databaseID = dbCfg.id;
        swaCfg.endPoint = "/api";
        swaCfg.name = "ConSys API Service";
        swaCfg.autoStart = true;
        service = (ConSysApiService) moduleRegistry.loadModule(swaCfg, TIMEOUT);
        apiRootUrl = httpServer.getPublicEndpointUrl(swaCfg.endPoint);

        // Create the OSHConnect instance
        oshConnect = new OSHConnect();
        node = oshConnect.createNode(httpServer.getServletsBaseUrl(), IS_SECURE, USERNAME, PASSWORD);

        var swe = new GeoPosHelper();
        dataRecord = swe.createRecord()
                .name("cat_sensor_data")
                .label("Cat Sensor Data")
                .description("Position data from the cat sensor.")
                .addField("time", swe.createTime()
                        .asSamplingTimeIsoUTC()
                        .label("Time")
                        .description("Time of data collection"))
                .addField("pos", swe.createLocationVectorLLA()
                        .label("Position"))
                .build();
    }

    @AfterEach
    void tearDown() {
        try {
            if (hub != null)
                hub.stop();
        } finally {
            if (dbFile != null)
                dbFile.delete();
        }

        oshConnect.shutdown();
    }

    protected SystemAdapter newSystem() {
        return newSystem("Cat Sensor", "A sensor that measures cats in the room.");
    }

    protected SystemAdapter newSystem(String name, String description) {
        var sys = new SMLHelper().createPhysicalSystem()
                .uniqueID("urn:sensor:cat_sensor_001")
                .name(name)
                .description(description)
                .build();


        return new SystemAdapter(sys);
    }

    protected DataStreamInfo newDataStreamInfo() {
        return newDataStreamInfo("Cat Sensor Datastream", "Position of cats in the room.");
    }

    protected DataStreamInfo newDataStreamInfo(String name, String description) {
        return new DataStreamInfo.Builder()
                .withSystem(FeatureId.NULL_FEATURE)
                .withName(name)
                .withDescription(description)
                .withRecordDescription(dataRecord)
                .withRecordEncoding(new TextEncodingImpl())
                .build();
    }
}
